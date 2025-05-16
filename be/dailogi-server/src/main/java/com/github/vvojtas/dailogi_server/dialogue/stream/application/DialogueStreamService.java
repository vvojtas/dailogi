package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.dialogue.api.CreateDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.application.DialogueCommandService;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.mapper.DialogueEventMapper;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.apikey.application.ApiKeyQueryService;
import com.github.vvojtas.dailogi_server.exception.NoApiKeyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service for streaming dialogue generation using Server-Sent Events.
 * Handles SSE connection setup and delegates generation logic to DialogueGenerationOrchestrator.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DialogueStreamService {

    private static final long SSE_TIMEOUT = 1800000L; // 30 minutes timeout
    
    private final CurrentUserService currentUserService;
    private final DialogueGenerationOrchestrator dialogueGenerationOrchestrator;
    private final DialogueEventMapper dialogueEventMapper;
    private final ApiKeyQueryService apiKeyQueryService;
    private final DialogueCommandService dialogueCommandService;
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
        log.info("Received request to stream dialogue for user {}", authentication.getName());

        // Create SseEmitter with timeout
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        try {
            // Get API key
            String apiKey = apiKeyQueryService.getDecryptedApiKey();
            if (apiKey == null) {
                throw new NoApiKeyException( "dialogue_generation", "API key is required for dialogue generation");
            }

            // Convert StreamDialogueCommand to CreateDialogueCommand
            CreateDialogueCommand createCommand = new CreateDialogueCommand(
                command.dialogueName(),
                command.sceneDescription(),
                command.characterConfigs(),
                false  // not global
            );
            
            // Create dialogue using DialogueCommandService
            DialogueDTO dialogueDTO = dialogueCommandService.createDialogue(createCommand);
            final long dialogueId = dialogueDTO.id();
            log.info("Created new dialogue entity: id={}, name={}", dialogueId, dialogueDTO.name());

            // Register the emitter so we can track it
            activeEmitters.put(dialogueId, emitter);
            log.debug("Emitter for dialogue {} registered. Active emitters: {}", dialogueId, activeEmitters.size());
            
            // Create callback for when the handler becomes inactive
            Consumer<Long> onInactivate = (id) -> {
                activeEmitters.remove(id);
                log.debug("Removed emitter for dialogue {} from active emitters. Remaining: {}", id, activeEmitters.size());
            };
                   
            
            // Create the event handler that will send events through SSE
            DialogueEventHandler eventHandler = new SseDialogueEventHandler(
                    dialogueId, 
                    emitter,
                    onInactivate,
                    dialogueEventMapper);

            // Start dialogue generation asynchronously using the event handler
            dialogueGenerationOrchestrator.generateDialogue(
                    dialogueDTO,
                    apiKey,
                    eventHandler);
            
            log.info("Delegated dialogue {} generation to orchestrator with event handler.", dialogueId);

            // Return emitter immediately to the client
            return emitter;

        } catch (Exception e) {
            log.error("Error setting up dialogue stream: {}", e.getMessage(), e);
            // Clean up if an error occurred during setup *before* async task started
            if (emitter != null) {
                emitter.completeWithError(e); // Complete emitter with error
            }
            return emitter;
        }
    }
} 