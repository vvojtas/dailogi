package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.*;
import com.github.vvojtas.dailogi_server.model.dialogue.mapper.DialogueEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Implementation of DialogueEventHandler that sends events via Server-Sent Events (SSE).
 * This class bridges the DialogueGenerationOrchestrator and the SseEmitter interface.
 * It maps API events to response model events before sending them to the client.
 */
@Slf4j
public class SseDialogueEventHandler implements DialogueEventHandler {

    private final long dialogueId;
    private final SseEmitter emitter;
    private final DialogueEventMapper eventMapper;
    private final Consumer<Long> onInactivate;
    
    // Thread-safe flag to track if this handler is still active
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    /**
     * Creates a new SSE dialogue event handler.
     *
     * @param dialogueId      The ID of the dialogue this handler is associated with
     * @param emitter         The SSE emitter to send events through
     * @param onInactivate    Callback to execute when this handler becomes inactive
     * @param eventMapper     Mapper to convert API events to response model events
     */
    public SseDialogueEventHandler(long dialogueId, SseEmitter emitter, Consumer<Long> onInactivate, DialogueEventMapper eventMapper) {
        this.dialogueId = dialogueId;
        this.emitter = emitter;
        this.onInactivate = onInactivate;
        this.eventMapper = eventMapper;
        
        // Set up completion callback to mark this handler as inactive and notify service
        emitter.onCompletion(() -> {
            log.info("SSE stream completed normally for dialogueId: {}", dialogueId);
            setInactive();
        });
        
        // Set up timeout callback to mark this handler as inactive and notify service
        emitter.onTimeout(() -> {
            log.warn("SSE stream timed out for dialogueId: {}", dialogueId);
            setInactive();
            
            // Optionally send a final error event if possible, but timeout usually means client disconnected
            try {
                // Check if emitter can still accept events (might be too late)
                emitter.send(SseEmitter.event().name("error").data(
                        new ErrorEventDto("Stream timed out on server", false, UUID.randomUUID().toString())));
                emitter.complete(); // Ensure completion after timeout
            } catch (Exception e) {
                log.debug("Could not send timeout error event for dialogueId {}: {}", dialogueId, e.getMessage());
            }
        });
        
        // Set up error callback to mark this handler as inactive and notify service
        emitter.onError(ex -> {
            log.error("SSE stream error for dialogueId: {}: {}", dialogueId, ex.getMessage(), ex);
            setInactive();
        });
    }

    /**
     * Checks if this handler is still active.
     * @return true if the handler is active, false otherwise
     */
    private boolean isEmitterActive() {
        boolean active = isActive.get();
        if (!active) {
            log.trace("Dialogue {} emitter is no longer active, events will not be sent", dialogueId);
        }
        return active;
    }
    
    /**
     * Marks this handler as inactive and notifies the service.
     */
    public void setInactive() {
        boolean wasActive = isActive.getAndSet(false);
        if (wasActive) {
            log.debug("Handler for dialogue {} marked as inactive", dialogueId);
            onInactivate.accept(dialogueId);
        }
    }

    /**
     * Sends an SSE event with the given name and data to the client.
     */
    private <T> void sendEvent(String eventName, T data) throws IOException {
        if (emitter == null) {
            log.warn("Attempted to send event '{}' to a null emitter for dialogue {}", eventName, dialogueId);
            return;
        }
        
        // Create SSE event object
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(eventName)
                .data(data);
        
        try {
            // Send the event immediately
            emitter.send(event);
            
            // Additional flush after each event - forces immediate delivery
            emitter.send(SseEmitter.event().comment(""));
        } catch (IOException e) {
            log.error("Failed to send event '{}' for dialogue {}: {}", eventName, dialogueId, e.getMessage());
            throw e; // Propagate to calling method for proper error handling
        }
    }

    @Override
    public void onDialogueStart(DialogueStartEventDto apiEvent) {
        if (!isEmitterActive()) return;
        
        try {
            // Map API event to response model event using fully qualified name
            com.github.vvojtas.dailogi_server.model.dialogue.response.event.DialogueStartEventDto mappedEvent = 
                    eventMapper.toDialogueStartEventDto(apiEvent);
            sendEvent("dialogue-start", mappedEvent);
            log.debug("Dialogue {}: Sent mapped dialogue-start event", dialogueId);
        } catch (IOException e) {
            log.error("Error sending dialogue-start event for dialogue {}: {}", dialogueId, e.getMessage(), e);
            // We don't complete the emitter here as this would be the first event
            // and should be sent from the DialogueStreamService directly
        }
    }

    @Override
    public void onCharacterStart(CharacterStartEventDto apiEvent) {
        if (!isEmitterActive()) return;
        
        try {
            // Map API event to response model event using fully qualified name
            com.github.vvojtas.dailogi_server.model.dialogue.response.event.CharacterStartEventDto mappedEvent = 
                    eventMapper.toCharacterStartEventDto(apiEvent);
            sendEvent("character-start", mappedEvent);
            log.debug("Dialogue {}: Sent mapped character-start event for character {}", 
                    dialogueId, apiEvent.getCharacterConfig().characterId());
        } catch (IOException e) {
            log.error("Error sending character-start event for dialogue {}: {}", dialogueId, e.getMessage(), e);
            completeWithError(e);
        }
    }

    @Override
    public void onToken(TokenEventDto apiEvent) {
        if (!isEmitterActive()) return;
        
        try {
            // Map API event to response model event using fully qualified name
            com.github.vvojtas.dailogi_server.model.dialogue.response.event.TokenEventDto mappedEvent = 
                    eventMapper.toTokenEventDto(apiEvent);
            sendEvent("token", mappedEvent);
            log.trace("Dialogue {}: Sent mapped token event for character {}", dialogueId, apiEvent.characterConfig().characterId());
        } catch (IOException e) {
            log.error("Error sending token event for dialogue {}: {}", dialogueId, e.getMessage(), e);
            completeWithError(e);
        }
    }

    @Override
    public void onCharacterComplete(CharacterCompleteEventDto apiEvent) {
        if (!isEmitterActive()) return;
        
        try {
            // Map API event to response model event using fully qualified name
            com.github.vvojtas.dailogi_server.model.dialogue.response.event.CharacterCompleteEventDto mappedEvent = 
                    eventMapper.toCharacterCompleteEventDto(apiEvent);
            sendEvent("character-complete", mappedEvent);
            log.debug("Dialogue {}: Sent mapped character-complete event for character {} ({} tokens)", 
                    dialogueId, apiEvent.characterId(), apiEvent.tokenCount());
        } catch (IOException e) {
            log.error("Error sending character-complete event for dialogue {}: {}", dialogueId, e.getMessage(), e);
            completeWithError(e);
        }
    }

    @Override
    public void onDialogueComplete(DialogueCompleteEventDto apiEvent) {
        if (!isEmitterActive()) return;
        
        try {
            // Map API event to response model event using fully qualified name
            com.github.vvojtas.dailogi_server.model.dialogue.response.event.DialogueCompleteEventDto mappedEvent = 
                    eventMapper.toDialogueCompleteEventDto(apiEvent);
            sendEvent("dialogue-complete", mappedEvent);
            log.info("Dialogue {}: Sent mapped dialogue-complete event", dialogueId);
            
            // Complete the emitter after sending the final event
            emitter.complete();
            log.debug("Dialogue {}: Emitter completed successfully", dialogueId);
            
            // Mark handler as inactive
            setInactive();
        } catch (IOException e) {
            log.error("Error sending dialogue-complete event for dialogue {}: {}", dialogueId, e.getMessage(), e);
            completeWithError(e);
        }
    }

    @Override
    public void onError(long dialogueId, Exception exception) {
        if (!isEmitterActive()) return;
        
        try {
            // Create API error event
            String errorEventId = UUID.randomUUID().toString();
            ErrorEventDto apiErrorEvent = new ErrorEventDto(
                    "Error during dialogue generation: " + exception.getMessage(),
                    false,
                    errorEventId);
            
            // Map to response model event using fully qualified name
            com.github.vvojtas.dailogi_server.model.dialogue.response.event.ErrorEventDto mappedEvent = 
                    eventMapper.toErrorEventDto(apiErrorEvent);
            sendEvent("error", mappedEvent);
            log.debug("Dialogue {}: Sent mapped error event", dialogueId);
        } catch (IOException e) {
            log.error("Error sending error event for dialogue {}: {}", dialogueId, e.getMessage(), e);
            // Fall through to completeWithError
        } finally {
            completeWithError(exception);
        }
    }
    
    /**
     * Completes the emitter with an error and marks the handler as inactive.
     */
    private void completeWithError(Exception e) {
        try {
            if (isEmitterActive()) {
                emitter.completeWithError(e);
                log.debug("Dialogue {}: Emitter completed with error", dialogueId);
                setInactive();
            }
        } catch (Exception ex) {
            log.error("Error completing emitter with error for dialogue {}: {}", dialogueId, ex.getMessage(), ex);
            // Last resort cleanup - ensure we're marked as inactive
            setInactive();
        }
    }
} 