package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.db.entity.DialogueStatus;
import com.github.vvojtas.dailogi_server.dialogue.api.DialogueMessageSaveCommand;
import com.github.vvojtas.dailogi_server.dialogue.application.DialogueMessageCommandService;
import com.github.vvojtas.dailogi_server.dialogue.application.DialogueStatusCommandService;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of DialogueEventHandler that persists dialogue events to the database.
 * This handler saves messages when characters complete their responses and updates
 * dialogue status when the dialogue is completed or encounters an error.
 * 
 * Note: This handler throws exceptions which should be caught by the calling code.
 */
@Slf4j
public class PersistenceDialogueEventHandler implements DialogueEventHandler {

    private final long dialogueId;
    private final DialogueMessageCommandService messageCommandService;
    private final DialogueStatusCommandService statusCommandService;

    /**
     * Creates a new persistence dialogue event handler.
     *
     * @param dialogueId           The ID of the dialogue this handler is associated with
     * @param messageCommandService The service for saving dialogue messages
     * @param statusCommandService  The service for updating dialogue status
     */
    public PersistenceDialogueEventHandler(
            long dialogueId, 
            DialogueMessageCommandService messageCommandService,
            DialogueStatusCommandService statusCommandService) {
        this.dialogueId = dialogueId;
        this.messageCommandService = messageCommandService;
        this.statusCommandService = statusCommandService;
        log.debug("Created PersistenceDialogueEventHandler for dialogue {}", dialogueId);
    }

    @Override
    public void onDialogueStart(DialogueStartEventDto event) {
        // Nothing to persist on dialogue start - dialogue is already created
    }

    @Override
    public void onCharacterStart(CharacterStartEventDto event) {
        // Nothing to persist on character start
    }

    @Override
    public void onToken(TokenEventDto event) {
        // Nothing to persist for individual tokens
    }

    @Override
    public void onCharacterComplete(CharacterCompleteEventDto event) {
        log.debug("Persisting message for character {} in dialogue {}", event.characterId(), dialogueId);
        
        // Create save command
        DialogueMessageSaveCommand saveCommand = new DialogueMessageSaveCommand(
            event.characterId(),
            event.messageContent(),
            event.messageSequenceNumber()
        );
        
        // Save message
        Long messageId = messageCommandService.saveMessage(dialogueId, saveCommand);
        log.info("Saved message ID {} for character {} in dialogue {}", 
                messageId, event.characterId(), dialogueId);
    }

    @Override
    public void onDialogueComplete(DialogueCompleteEventDto event) {
        log.debug("Updating dialogue {} status to COMPLETED", dialogueId);
        boolean updated = statusCommandService.updateStatus(dialogueId, DialogueStatus.COMPLETED);
        if (updated) {
            log.info("Updated dialogue {} status to COMPLETED", dialogueId);
        } else {
            log.warn("Failed to update dialogue {} status to COMPLETED", dialogueId);
        }
    }

    @Override
    public void onError(long dialogueId, Exception exception) {
        log.debug("Updating dialogue {} status to FAILED due to error", dialogueId);
        boolean updated = statusCommandService.updateStatus(dialogueId, DialogueStatus.FAILED);
        if (updated) {
            log.info("Updated dialogue {} status to FAILED", dialogueId);
        } else {
            log.warn("Failed to update dialogue {} status to FAILED", dialogueId);
        }
    }
} 