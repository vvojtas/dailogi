package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DialogueEventHandler that delegates event handling to multiple handlers.
 * This allows for separation of concerns, such as having one handler for SSE streaming
 * and another for database persistence.
 * 
 * This composite handler also provides centralized error handling for all delegated handlers.
 * The handler captures the security context at creation time and restores it before delegating
 * to child handlers, ensuring authentication context is preserved across thread boundaries.
 */
@Slf4j
public class CompositeDialogueEventHandler implements DialogueEventHandler {

    private final List<DialogueEventHandler> handlers;
    private final long dialogueId;
    private final SecurityContext storedSecurityContext;

    /**
     * Creates a new composite dialogue event handler.
     *
     * @param dialogueId The ID of the dialogue this handler is associated with
     * @param handlers   The list of handlers to delegate to
     */
    public CompositeDialogueEventHandler(long dialogueId, List<DialogueEventHandler> handlers) {
        this.dialogueId = dialogueId;
        this.handlers = new ArrayList<>(handlers); // Create a defensive copy
        // Capture the security context at creation time when it's still available
        this.storedSecurityContext = SecurityContextHolder.getContext();
        log.debug("Created CompositeDialogueEventHandler with {} handlers for dialogue {}", 
                handlers.size(), dialogueId);
    }

    @Override
    public void onDialogueStart(DialogueStartEventDto event) {
        for (DialogueEventHandler handler : handlers) {
            // Save the current context before setting stored context
            SecurityContext originalContext = SecurityContextHolder.getContext();
            try {
                // Set the stored context before delegating
                SecurityContextHolder.setContext(storedSecurityContext);
                handler.onDialogueStart(event);
            } catch (AccessDeniedException e) {
                logAccessDenied(handler, "dialogue start", e);
                // Continue with other handlers
            } catch (Exception e) {
                logError(handler, "dialogue start", e);
                // Continue with other handlers
            } finally {
                // Restore the original context
                SecurityContextHolder.setContext(originalContext);
            }
        }
    }

    @Override
    public void onCharacterStart(CharacterStartEventDto event) {
        for (DialogueEventHandler handler : handlers) {
            // Save the current context before setting stored context
            SecurityContext originalContext = SecurityContextHolder.getContext();
            try {
                // Set the stored context before delegating
                SecurityContextHolder.setContext(storedSecurityContext);
                handler.onCharacterStart(event);
            } catch (AccessDeniedException e) {
                logAccessDenied(handler, "character start for character " + 
                        event.getCharacterConfig().characterId(), e);
                // Continue with other handlers
            } catch (Exception e) {
                logError(handler, "character start for character " + 
                        event.getCharacterConfig().characterId(), e);
                // Continue with other handlers
            } finally {
                // Restore the original context
                SecurityContextHolder.setContext(originalContext);
            }
        }
    }

    @Override
    public void onToken(TokenEventDto event) {
        for (DialogueEventHandler handler : handlers) {
            // Save the current context before setting stored context
            SecurityContext originalContext = SecurityContextHolder.getContext();
            try {
                // Set the stored context before delegating
                SecurityContextHolder.setContext(storedSecurityContext);
                handler.onToken(event);
            } catch (AccessDeniedException e) {
                logAccessDenied(handler, "token for character " + 
                        event.characterConfig().characterId(), e);
                // Continue with other handlers
            } catch (Exception e) {
                logError(handler, "token for character " + 
                        event.characterConfig().characterId(), e);
                // Continue with other handlers
            } finally {
                // Restore the original context
                SecurityContextHolder.setContext(originalContext);
            }
        }
    }

    @Override
    public void onCharacterComplete(CharacterCompleteEventDto event) {
        for (DialogueEventHandler handler : handlers) {
            // Save the current context before setting stored context
            SecurityContext originalContext = SecurityContextHolder.getContext();
            try {
                // Set the stored context before delegating
                SecurityContextHolder.setContext(storedSecurityContext);
                handler.onCharacterComplete(event);
            } catch (AccessDeniedException e) {
                logAccessDenied(handler, "character complete for character " + 
                        event.characterId(), e);
                // Continue with other handlers
            } catch (Exception e) {
                logError(handler, "character complete for character " + 
                        event.characterId(), e);
                // Continue with other handlers
            } finally {
                // Restore the original context
                SecurityContextHolder.setContext(originalContext);
            }
        }
    }

    @Override
    public void onDialogueComplete(DialogueCompleteEventDto event) {
        for (DialogueEventHandler handler : handlers) {
            // Save the current context before setting stored context
            SecurityContext originalContext = SecurityContextHolder.getContext();
            try {
                // Set the stored context before delegating
                SecurityContextHolder.setContext(storedSecurityContext);
                handler.onDialogueComplete(event);
            } catch (AccessDeniedException e) {
                logAccessDenied(handler, "dialogue complete", e);
                // Continue with other handlers
            } catch (Exception e) {
                logError(handler, "dialogue complete", e);
                // Continue with other handlers
            } finally {
                // Restore the original context
                SecurityContextHolder.setContext(originalContext);
            }
        }
    }

    @Override
    public void onError(long dialogueId, Exception exception) {
        for (DialogueEventHandler handler : handlers) {
            // Save the current context before setting stored context
            SecurityContext originalContext = SecurityContextHolder.getContext();
            try {
                // Set the stored context before delegating
                SecurityContextHolder.setContext(storedSecurityContext);
                handler.onError(dialogueId, exception);
            } catch (AccessDeniedException e) {
                logAccessDenied(handler, "error handling", e);
                // Continue with other handlers
            } catch (Exception e) {
                logError(handler, "error handling", e);
                // Continue with other handlers
            } finally {
                // Restore the original context
                SecurityContextHolder.setContext(originalContext);
            }
        }
    }
    
    /**
     * Helper method to log access denied exceptions
     */
    private void logAccessDenied(DialogueEventHandler handler, String eventType, AccessDeniedException e) {
        log.warn("Access denied for handler {} during {} event in dialogue {}: {}", 
                handler.getClass().getSimpleName(), 
                eventType,
                dialogueId, 
                e.getMessage());
    }
    
    /**
     * Helper method to log general exceptions
     */
    private void logError(DialogueEventHandler handler, String eventType, Exception e) {
        log.error("Handler {} failed during {} event in dialogue {}: {}", 
                handler.getClass().getSimpleName(), 
                eventType,
                dialogueId, 
                e.getMessage(), e);
    }
} 