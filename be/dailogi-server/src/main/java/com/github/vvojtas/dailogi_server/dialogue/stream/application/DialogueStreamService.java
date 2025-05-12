package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.character.application.CharacterQueryService;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.CharacterConfigDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterCompleteEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueCompleteEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueStartEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.ErrorEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.TokenEventDto;
import com.github.vvojtas.dailogi_server.llm.application.LLMQueryService;
import com.github.vvojtas.dailogi_server.llm.application.OpenRouterMock;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;

/**
 * Service for streaming dialogue generation using Server-Sent Events
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DialogueStreamService {

    private static final long SSE_TIMEOUT = 300000L; // 5 minutes timeout
    private static final int DEFAULT_LENGTH = 5; // Default number of dialogue turns
    
    private final CharacterQueryService characterQueryService;
    private final LLMQueryService llmQueryService;
    private final OpenRouterMock openRouterMock;
    private final CurrentUserService currentUserService;
    // Store active emitters to be able to close them if needed
    private final Map<Long, SseEmitter> activeEmitters = new ConcurrentHashMap<>();
    
    /**
     * Starts a dialogue stream using Server-Sent Events
     *
     * @param command The command containing dialogue configuration
     * @param authentication The current user's authentication
     * @return SseEmitter for streaming the dialogue generation
     */
    @Transactional
    public SseEmitter streamDialogue(StreamDialogueCommand command, Authentication authentication) {
        AppUser currentUser = currentUserService.getCurrentAppUser();
        
        // Create SseEmitter with timeout
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        try {
            // Generate unique dialogue ID
            final long dialogueId = ThreadLocalRandom.current().nextLong(11, 1001);
            
            // Setup emitter callbacks
            emitter.onCompletion(() -> {
                log.info("SSE stream completed for user {}", currentUser.getId());
                // Remove from active emitters
                activeEmitters.remove(dialogueId);
            });
            
            emitter.onTimeout(() -> {
                log.info("SSE stream timed out for user {}", currentUser.getId());
                // Handle timeout, notify client if possible
                try {
                    sendErrorEvent(emitter, "Stream timed out", false);
                } catch (IOException e) {
                    log.error("Error sending timeout notification", e);
                }
            });
            
            emitter.onError(ex -> {
                log.error("SSE stream error for user {}: {}", currentUser.getId(), ex.getMessage());
                // Error handling
            });
            
            // Store emitter for potential cancellation
            activeEmitters.put(dialogueId, emitter);
            
            // Load character and LLM entities, validate character ownership
            Map<Long, CharacterDTO> characters = new HashMap<>();
            Map<Long, LLMDTO> llms = new HashMap<>();
            loadEntities(command.characterConfigs(), characters, llms, currentUser);
            

            // Send initial dialogue start event
            sendEvent(emitter, "dialogue-start", 
                    new DialogueStartEventDto(dialogueId, command.characterConfigs(), 0));
            
            // Start dialogue generation in a separate thread to not block the request
            new Thread(() -> generateDialogue(dialogueId, emitter, command, characters, llms, authentication))
                    .start();
            
            return emitter;
            
        } catch (Exception e) {
            log.error("Error starting dialogue stream", e);
            emitter.completeWithError(e);
            return emitter;
        }
    }

    
    /**
     * Validates character configurations and loads related entities
     */
    private void loadEntities(
            List<CharacterConfigDto> configs,
            Map<Long, CharacterDTO> characters,
            Map<Long, LLMDTO> llms,
            AppUser user) {
        
        for (CharacterConfigDto config : configs) {
            // Check if character exists and user has access
            CharacterDTO character = characterQueryService.getCharacter(config.characterId());
            characters.put(character.id(), character);
            
            // Check if LLM exists
            LLMDTO llm = llmQueryService.findById(config.llmId());
            llms.put(llm.id(), llm);
        }
    }
    
    
    /**
     * Generates the dialogue by prompting the OpenRouter mock for each character turn
     */
    private void generateDialogue(
            long dialogueId,
            SseEmitter emitter,
            StreamDialogueCommand command,
            Map<Long, CharacterDTO> characters,
            Map<Long, LLMDTO> llms,
            Authentication authentication
           ) {
        // Na początku metody ustaw kontekst bezpieczeństwa
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        try {
            // Use command length or default if not provided
            int turnCount = command.length() != null ? command.length() : DEFAULT_LENGTH;
            
            // For each turn, have each character generate a response
            for (int turn = 0; turn < turnCount; turn++) {
                if (!activeEmitters.containsKey(dialogueId)) {
                    log.debug("Emitter completed, stopping dialogue generation");
                    break;
                }
                
                for (CharacterConfigDto config : command.characterConfigs()) {
                    CharacterDTO character = characters.get(config.characterId());
                    LLMDTO llm = llms.get(config.llmId());
                    
                    // Prepare a simple prompt
                    String prompt = String.format(
                            "Continue the conversation as %s. Topic: %s",
                            character.name(),
                            command.sceneDescription());
                    
                    // Track tokens using a wrapper to make it effectively final
                    final int[] tokenCountWrapper = new int[1];
                    
                    // Generate text using OpenRouter mock
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    
                    openRouterMock.generateText(
                            llm.openrouterIdentifier(),
                            prompt,
                            token -> {
                                try {
                                    tokenCountWrapper[0]++;
                                    // Send token event
                                    String eventId = UUID.randomUUID().toString();
                                    sendEvent(emitter, "token", new TokenEventDto(
                                            character.id(), token, eventId));
                                    
                                } catch (Exception e) {
                                    log.error("Error sending token", e);
                                }
                            },
                            () -> {
                                try {
                                    // Character's turn is complete
                                    String completeId = UUID.randomUUID().toString();
                                    
                                    // Send character complete event
                                    sendEvent(emitter, "character-complete", new CharacterCompleteEventDto(
                                            character.id(), tokenCountWrapper[0], completeId));
                                    
                                } catch (Exception e) {
                                    log.error("Error handling character completion", e);
                                }
                                future.complete(null);
                            }
                    );
                    
                    future.join();
                }
            }
            
            // All turns completed, send dialogue complete event
            String dialogueCompleteId = UUID.randomUUID().toString();
            sendEvent(emitter, "dialogue-complete", new DialogueCompleteEventDto(
                    "completed", turnCount, dialogueCompleteId));
            
            log.info("Dialogue complete event sent");
            // Complete the emitter
            emitter.complete();
            
        } catch (Exception e) {
            log.error("Error during dialogue generation", e);
            try {
                // Send error event and complete with error
                sendErrorEvent(emitter, "Error during dialogue generation: " + e.getMessage(), false);
                emitter.completeWithError(e);
            } catch (IOException ex) {
                log.error("Failed to send error event", ex);
            }
        } finally {
            // Clean up resources
            activeEmitters.remove(dialogueId);
        }
    }
    
    /**
     * Sends an SSE event with the given name and data
     */
    private <T> void sendEvent(SseEmitter emitter, String eventName, T data) throws IOException {
        emitter.send(SseEmitter.event()
                .name(eventName)
                .data(data));
    }
    
    /**
     * Sends an error event
     */
    private void sendErrorEvent(SseEmitter emitter, String message, boolean recoverable) throws IOException {
        sendEvent(emitter, "error", new ErrorEventDto(
                message, recoverable, UUID.randomUUID().toString()));
    }
} 