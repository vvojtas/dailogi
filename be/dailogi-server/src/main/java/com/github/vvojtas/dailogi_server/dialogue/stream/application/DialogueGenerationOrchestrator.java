package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
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
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueMessageDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
     * @param dialogueDTO   The dialogue data transfer object
     * @param apiKey        The decrypted API key for OpenRouter
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

            // Initialize message history list if not already present
            List<DialogueMessageDTO> messageHistory = new ArrayList<>(dialogueDTO.messages() != null ? dialogueDTO.messages() : new ArrayList<>());

            // For each turn, have each character generate a response sequentially
            for (int turn = 0; turn < turnCount; turn++) {
                final int currentTurn = turn; // Make effectively final for lambda capture
                log.debug("Dialogue {} starting turn {}.", dialogueDTO.id(), currentTurn + 1);

                for (DialogueCharacterConfigDTO config : dialogueDTO.characterConfigs()) {

                    CharacterDTO character = config.character();
                    LLMDTO llm = config.llm();
                    CharacterConfigDTO characterConfig = config.toCharacterConfigDTO();
                    log.debug("Dialogue {} turn {}: Character {} ({}) using LLM {} starts generation.",
                            dialogueDTO.id(), currentTurn + 1, character.name(), character.id(), llm.openrouterIdentifier());

                    // Send character start event
                    try {
                        String startEventId = UUID.randomUUID().toString();
                        eventHandler.onCharacterStart(new CharacterStartEventDto(characterConfig, startEventId));
                        log.trace("Dialogue {} turn {}: Sent character-start event for character {}", dialogueDTO.id(), currentTurn + 1, character.id());
                    } catch (Exception e) {
                        log.error("Dialogue {} turn {}: Failed to send character-start event for character {}. Error: {}", 
                                dialogueDTO.id(), currentTurn + 1, character.id(), e.getMessage(), e);
                        throw new RuntimeException("Failed to send character-start event", e); // Propagate to main catch block
                    }

                    // Build prompts using the prompt builder and updated message history
                    DialogueDTO currentDialogueState = new DialogueDTO(
                        dialogueDTO.id(),
                        dialogueDTO.name(),
                        dialogueDTO.sceneDescription(),
                        dialogueDTO.status(),
                        dialogueDTO.createdAt(),
                        dialogueDTO.updatedAt(),
                        dialogueDTO.characterConfigs(),
                        messageHistory
                    );
                    
                    List<ChatMessage> messages = promptBuilder.buildDialogueMessages(currentDialogueState, config);
                    
                    final int[] tokenCountWrapper = new int[1];
                    final StringBuilder messageContentBuilder = new StringBuilder();
                    CompletableFuture<Void> future = new CompletableFuture<>();

                    openRouterInterface.streamChat(
                            llm.openrouterIdentifier(),
                            messages,
                            apiKey,
                            token -> handleToken(dialogueDTO.id(), characterConfig, token, tokenCountWrapper, messageContentBuilder, eventHandler),
                            () -> handleCharacterCompletion(dialogueDTO.id(), character.id(), character.name(), tokenCountWrapper[0], messageContentBuilder.toString(), messageHistory, currentTurn + 1, future, eventHandler)
                    );

                    // Wait for the current character's generation to complete before moving to the next
                    future.join();
                    log.debug("Dialogue {} turn {}: Character {} ({}) finished generation.",
                            dialogueDTO.id(), currentTurn + 1, character.name(), character.id());
                } // End character loop

                log.debug("Dialogue {} finished turn {}.", dialogueDTO.id(), currentTurn + 1);
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

    private void handleToken(long dialogueId, CharacterConfigDTO config, String token, int[] tokenCountWrapper, StringBuilder messageBuilder, DialogueEventHandler eventHandler) {
        try {
            tokenCountWrapper[0]++;
            // Append token to message builder
            messageBuilder.append(token);
            
            String eventId = UUID.randomUUID().toString();
            log.trace("Dialogue {} sending token event for character {}", dialogueId, config.characterId());
            
            eventHandler.onToken(new TokenEventDto(config, token, eventId));
        } catch (Exception e) {
            log.error("Error sending token event for dialogue {}, character {}", dialogueId, config.characterId(), e);
            // Notify the event handler about the error
            eventHandler.onError(dialogueId, e);
        }
    }

    private void handleCharacterCompletion(
            long dialogueId, 
            long characterId, 
            String characterName,
            int tokenCount, 
            String messageContent,
            List<DialogueMessageDTO> messageHistory,
            int turnNumber,
            CompletableFuture<Void> future, 
            DialogueEventHandler eventHandler) {
        try {
            String completeId = UUID.randomUUID().toString();
            log.debug("Dialogue {} sending character-complete event for character {} ({} tokens).", dialogueId, characterId, tokenCount);
            
            // Create a new message and add it to the history for in-memory use
            DialogueMessageDTO newMessage = new DialogueMessageDTO(
                (long) (messageHistory.size() + 1), // Generate next message ID
                turnNumber,
                characterId,
                messageContent
            );
            
            // Add to message history
            messageHistory.add(newMessage);
            log.debug("Added message from character {} to history. Total messages: {}", characterId, messageHistory.size());
            
            // Send character complete event with message sequence number
            // Persistence will be handled by PersistenceDialogueEventHandler
            eventHandler.onCharacterComplete(new CharacterCompleteEventDto(
                characterId, 
                tokenCount, 
                messageContent,
                turnNumber, 
                completeId
            ));
        } catch (Exception e) {
            log.error("Error sending character-complete event for dialogue {}, character {}", dialogueId, characterId, e);
            eventHandler.onError(dialogueId, e);
        } finally {
            // Always complete the future so the main loop can continue/finish.
            future.complete(null);
        }
    }
}