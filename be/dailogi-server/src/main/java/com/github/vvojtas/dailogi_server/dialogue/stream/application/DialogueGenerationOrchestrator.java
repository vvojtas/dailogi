package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.apikey.application.ApiKeyQueryService;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterCompleteEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterStartEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueCompleteEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.TokenEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueStartEventDto;
import com.github.vvojtas.dailogi_server.generation.api.ChatMessage;
import com.github.vvojtas.dailogi_server.generation.api.OpenRouterInterface;
import com.github.vvojtas.dailogi_server.generation.application.OpenRouterPromptBuilder;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueCharacterConfigDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DialogueGenerationOrchestrator {

    private static final int DEFAULT_LENGTH = 5; // Default number of dialogue turns (consider making this configurable or passed)

    private final OpenRouterInterface openRouterInterface;
    private final OpenRouterPromptBuilder promptBuilder;

    /**
     * Asynchronously generates the dialogue by prompting the OpenRouter for each character turn.
     * This method runs in a separate thread managed by Spring's TaskExecutor.
     * Events are delivered through the provided event handler.
     *
     * @param dialogueId    Unique ID for the dialogue session.
     * @param command       The command containing dialogue configuration.
     * @param characters    Map of character IDs to CharacterDTOs involved in the dialogue.
     * @param llms          Map of LLM IDs to LLMDTOs used by the characters.
     * @param apiKey        The decrypted API key for OpenRouter.
     * @param eventHandler  Handler for dialogue generation events (token generation, completion, etc.)
     */
    @Async // Marks this method for asynchronous execution
    public void generateDialogue(DialogueDTO dialogueDTO, String apiKey, DialogueEventHandler eventHandler) {

        log.info("Starting asynchronous dialogue generation for dialogueId: {}", dialogueDTO.id());
        // SecurityContext is automatically propagated by Spring when using @Async

        try {  
            // Send dialogue start event
            int turnCount = DEFAULT_LENGTH;
            List<CharacterConfigDTO> characterConfigs = dialogueDTO.characterConfigs().stream().map(DialogueCharacterConfigDTO::toCharacterConfigDTO).toList();
            eventHandler.onDialogueStart(new DialogueStartEventDto(
                dialogueDTO.id(), 
                characterConfigs,
                turnCount));
            log.debug("Dialogue {} start event sent", dialogueDTO.id());
            
            log.debug("Dialogue {} will have {} turns.", dialogueDTO.id(), turnCount);

            // For each turn, have each character generate a response sequentially
            for (int turn = 0; turn < turnCount; turn++) {
                log.debug("Dialogue {} starting turn {}.", dialogueDTO.id(), turn + 1);

                for (DialogueCharacterConfigDTO config : dialogueDTO.characterConfigs()) {

                    CharacterDTO character = config.character();
                    LLMDTO llm = config.llm();
                    CharacterConfigDTO characterConfig = config.toCharacterConfigDTO();
                    log.debug("Dialogue {} turn {}: Character {} ({}) using LLM {} starts generation.",
                            dialogueDTO.id(), turn + 1, character.name(), character.id(), llm.openrouterIdentifier());

                    // Send character start event
                    try {
                        String startEventId = UUID.randomUUID().toString();
                        eventHandler.onCharacterStart(new CharacterStartEventDto(characterConfig, startEventId));
                        log.trace("Dialogue {} turn {}: Sent character-start event for character {}", dialogueDTO.id(), turn + 1, character.id());
                    } catch (Exception e) {
                        log.error("Dialogue {} turn {}: Failed to send character-start event for character {}. Error: {}", 
                                dialogueDTO.id(), turn + 1, character.id(), e.getMessage(), e);
                        throw new RuntimeException("Failed to send character-start event", e); // Propagate to main catch block
                    }

                    // Build prompts using the prompt builder
                    List<ChatMessage> messages = promptBuilder.buildDialogueMessages(dialogueDTO, config);
                    
                    final int[] tokenCountWrapper = new int[1];
                    CompletableFuture<Void> future = new CompletableFuture<>();

                    openRouterInterface.streamChat(
                            llm.openrouterIdentifier(),
                            messages,
                            apiKey,
                            token -> handleToken(dialogueDTO.id(), characterConfig, token, tokenCountWrapper, eventHandler),
                            () -> handleCharacterCompletion(dialogueDTO.id(), character.id(), tokenCountWrapper[0], future, eventHandler)
                    );

                    // Wait for the current character's generation to complete before moving to the next
                    future.join();
                    log.debug("Dialogue {} turn {}: Character {} ({}) finished generation.",
                            dialogueDTO.id(), turn + 1, character.name(), character.id());
                } // End character loop

                log.debug("Dialogue {} finished turn {}.", dialogueDTO.id(), turn + 1);
            } // End turn loop

            String dialogueCompleteId = UUID.randomUUID().toString();
            log.info("Dialogue {} generation finished. Sending dialogue-complete event.", dialogueDTO.id());
            eventHandler.onDialogueComplete(new DialogueCompleteEventDto(
                    "completed", turnCount, dialogueCompleteId)); // Use the calculated turnCount
            log.debug("Dialogue {} completed successfully.", dialogueDTO.id());

        } catch (Exception e) {
            log.error("Error during asynchronous dialogue generation for dialogueId {}", dialogueDTO.id(), e);
            // Notify the event handler about the error
            eventHandler.onError(dialogueDTO.id(), e);
        }
        log.info("Finished asynchronous dialogue generation task for dialogueId: {}", dialogueDTO.id());
    }


    // --- Helper methods for handling generation steps and sending events ---

    private void handleToken(long dialogueId, CharacterConfigDTO config, String token, int[] tokenCountWrapper, DialogueEventHandler eventHandler) {
        try {
            tokenCountWrapper[0]++;
            String eventId = UUID.randomUUID().toString();
            log.trace("Dialogue {} sending token event for character {}", dialogueId, config.characterId());
            
            eventHandler.onToken(new TokenEventDto(config, token, eventId));
        } catch (Exception e) {
            log.error("Error sending token event for dialogue {}, character {}", dialogueId, config.characterId(), e);
            // Notify the event handler about the error
            eventHandler.onError(dialogueId, e);
        }
    }

    private void handleCharacterCompletion(long dialogueId, long characterId, int tokenCount, CompletableFuture<Void> future, DialogueEventHandler eventHandler) {
        try {
            String completeId = UUID.randomUUID().toString();
            log.debug("Dialogue {} sending character-complete event for character {} ({} tokens).", dialogueId, characterId, tokenCount);
            eventHandler.onCharacterComplete(new CharacterCompleteEventDto(characterId, tokenCount, completeId));
        } catch (Exception e) {
            log.error("Error sending character-complete event for dialogue {}, character {}", dialogueId, characterId, e);
            eventHandler.onError(dialogueId, e);
        } finally {
            // Always complete the future so the main loop can continue/finish.
            future.complete(null);
        }
    }
}