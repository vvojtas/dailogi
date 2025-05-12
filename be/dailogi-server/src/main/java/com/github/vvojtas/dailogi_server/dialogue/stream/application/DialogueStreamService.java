package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.character.application.CharacterQueryService;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.CharacterConfigDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.llm.application.LLMQueryService;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.mapper.DialogueEventMapper;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
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
    
    private final CharacterQueryService characterQueryService;
    private final LLMQueryService llmQueryService;
    private final CurrentUserService currentUserService;
    private final DialogueGenerationOrchestrator dialogueGenerationOrchestrator;
    private final DialogueEventMapper dialogueEventMapper;
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
        final long dialogueId = ThreadLocalRandom.current().nextLong(1, 1001);
        log.info("Received request to stream dialogue {} for user {}", dialogueId, currentUser.getId());

        // Create SseEmitter with timeout
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        try {
            // Register the emitter so we can track it
            activeEmitters.put(dialogueId, emitter);
            log.debug("Emitter for dialogue {} registered. Active emitters: {}", dialogueId, activeEmitters.size());
            
            // Create callback for when the handler becomes inactive
            Consumer<Long> onInactivate = (id) -> {
                activeEmitters.remove(id);
                log.debug("Removed emitter for dialogue {} from active emitters. Remaining: {}", id, activeEmitters.size());
            };
            
            // Load character and LLM entities, validate character ownership
            Map<Long, CharacterDTO> characters = new HashMap<>();
            Map<Long, LLMDTO> llms = new HashMap<>();
            loadEntities(command.characterConfigs(), characters, llms, currentUser);
            log.debug("Entities loaded for dialogue {}", dialogueId);

            // Create the event handler that will send events through SSE
            DialogueEventHandler eventHandler = new SseDialogueEventHandler(
                    dialogueId, 
                    emitter,
                    onInactivate,
                    dialogueEventMapper);

            // Start dialogue generation asynchronously using the event handler
            dialogueGenerationOrchestrator.generateDialogue(
                    dialogueId, 
                    command, 
                    characters, 
                    llms, 
                    eventHandler);
            
            log.info("Delegated dialogue {} generation to orchestrator with event handler.", dialogueId);

            // Return emitter immediately to the client
            return emitter;

        } catch (Exception e) {
            log.error("Error setting up dialogue stream for dialogueId {}: {}", dialogueId, e.getMessage(), e);
            // Clean up if an error occurred during setup *before* async task started
            activeEmitters.remove(dialogueId);
            emitter.completeWithError(e); // Complete emitter with error
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
        log.debug("Loading entities for user {}", user.getId());
        // Consider adding more specific exception handling here if needed
        for (CharacterConfigDto config : configs) {
            // Check if character exists and user has access
            CharacterDTO character = characterQueryService.getCharacter(config.characterId());
            characters.put(character.id(), character);
            
            // Check if LLM exists
            LLMDTO llm = llmQueryService.findById(config.llmId());
            llms.put(llm.id(), llm);
             log.trace("Loaded character {} and LLM {} for config.", character.id(), llm.id());
        }
         log.debug("Finished loading entities.");
    }
} 