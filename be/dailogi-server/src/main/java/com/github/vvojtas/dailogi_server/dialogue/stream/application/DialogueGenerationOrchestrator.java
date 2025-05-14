package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.CharacterConfigDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterCompleteEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterStartEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueCompleteEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.TokenEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueStartEventDto;
import com.github.vvojtas.dailogi_server.llm.application.OpenRouterMock;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class DialogueGenerationOrchestrator {

    private static final int DEFAULT_LENGTH = 5; // Default number of dialogue turns (consider making this configurable or passed)

    private final OpenRouterMock openRouterMock;

    /**
     * Asynchronously generates the dialogue by prompting the OpenRouter mock for each character turn.
     * This method runs in a separate thread managed by Spring's TaskExecutor.
     * Events are delivered through the provided event handler.
     *
     * @param dialogueId    Unique ID for the dialogue session.
     * @param command       The command containing dialogue configuration.
     * @param characters    Map of character IDs to CharacterDTOs involved in the dialogue.
     * @param llms          Map of LLM IDs to LLMDTOs used by the characters.
     * @param eventHandler  Handler for dialogue generation events (token generation, completion, etc.)
     */
    @Async // Marks this method for asynchronous execution
    public void generateDialogue(
            long dialogueId,
            StreamDialogueCommand command,
            Map<Long, CharacterDTO> characters,
            Map<Long, LLMDTO> llms,
            DialogueEventHandler eventHandler) {

        log.info("Starting asynchronous dialogue generation for dialogueId: {}", dialogueId);
        // SecurityContext is automatically propagated by Spring when using @Async

        try {
            // Send dialogue start event
            int turnCount = command.length() != null ? command.length() : DEFAULT_LENGTH;
            eventHandler.onDialogueStart(new DialogueStartEventDto(dialogueId, command.characterConfigs(), turnCount));
            log.debug("Dialogue {} start event sent", dialogueId);
            
            log.debug("Dialogue {} will have {} turns.", dialogueId, turnCount);

            // For each turn, have each character generate a response sequentially
            for (int turn = 0; turn < turnCount; turn++) {
                log.debug("Dialogue {} starting turn {}.", dialogueId, turn + 1);

                for (CharacterConfigDto config : command.characterConfigs()) {

                    CharacterDTO character = characters.get(config.characterId());
                    LLMDTO llm = llms.get(config.llmId());
                    log.debug("Dialogue {} turn {}: Character {} ({}) using LLM {} starts generation.",
                            dialogueId, turn + 1, character.name(), character.id(), llm.openrouterIdentifier());

                    // Send character start event
                    try {
                        String startEventId = UUID.randomUUID().toString();
                        eventHandler.onCharacterStart(new CharacterStartEventDto(config, startEventId));
                        log.trace("Dialogue {} turn {}: Sent character-start event for character {}", dialogueId, turn + 1, character.id());
                    } catch (Exception e) {
                        log.error("Dialogue {} turn {}: Failed to send character-start event for character {}. Error: {}", 
                                dialogueId, turn + 1, character.id(), e.getMessage(), e);
                        throw new RuntimeException("Failed to send character-start event", e); // Propagate to main catch block
                    }

                    String prompt = String.format(
                            "Continue the conversation as %s. Topic: %s",
                            character.name(),
                            command.sceneDescription());

                    final int[] tokenCountWrapper = new int[1];
                    CompletableFuture<Void> future = new CompletableFuture<>();

                    openRouterMock.generateText(
                            llm.openrouterIdentifier(),
                            prompt,
                            token -> handleToken(dialogueId, config, token, tokenCountWrapper, eventHandler),
                            () -> handleCharacterCompletion(dialogueId, character.id(), tokenCountWrapper[0], future, eventHandler)
                    );

                    // Wait for the current character's generation to complete before moving to the next
                    future.join();
                    log.debug("Dialogue {} turn {}: Character {} ({}) finished generation.",
                            dialogueId, turn + 1, character.name(), character.id());
                } // End character loop

                log.debug("Dialogue {} finished turn {}.", dialogueId, turn + 1);
            } // End turn loop

            String dialogueCompleteId = UUID.randomUUID().toString();
            log.info("Dialogue {} generation finished. Sending dialogue-complete event.", dialogueId);
            eventHandler.onDialogueComplete(new DialogueCompleteEventDto(
                    "completed", turnCount, dialogueCompleteId)); // Use the calculated turnCount
            log.debug("Dialogue {} completed successfully.", dialogueId);

        } catch (Exception e) {
            log.error("Error during asynchronous dialogue generation for dialogueId {}", dialogueId, e);
            // Notify the event handler about the error
            eventHandler.onError(dialogueId, e);
        }
        log.info("Finished asynchronous dialogue generation task for dialogueId: {}", dialogueId);
    }


    // --- Helper methods for handling generation steps and sending events ---

    private void handleToken(long dialogueId, CharacterConfigDto config, String token, int[] tokenCountWrapper, DialogueEventHandler eventHandler) {
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