package com.github.vvojtas.dailogi_server.model.dialogue.mapper;

// Target DTOs (where we are mapping TO)
import com.github.vvojtas.dailogi_server.model.dialogue.response.event.*;
// Source DTOs are used with fully qualified names in method signatures

import org.springframework.stereotype.Component;

@Component
public class DialogueEventMapper {

    // Identity mappings as source and target are the same after refactoring
    // Establishing the pattern for potential future changes

    public DialogueStartEventDto toDialogueStartEventDto(com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueStartEventDto apiEvent) {
        if (apiEvent == null) {
            return null;
        }
        // Assuming CharacterConfigDto doesn't need mapping for now, or its mapping is handled elsewhere
        return new DialogueStartEventDto(
                apiEvent.dialogueId(), 
                apiEvent.characterConfigs(), 
                apiEvent.turnCount()
        );
    }

    public CharacterStartEventDto toCharacterStartEventDto(com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterStartEventDto apiEvent) {
        if (apiEvent == null) {
            return null;
        }
         // Assuming CharacterConfigDto doesn't need mapping for now
        return new CharacterStartEventDto(
                apiEvent.getCharacterConfig(), 
                apiEvent.getEventId()
        );
    }

    public TokenEventDto toTokenEventDto(com.github.vvojtas.dailogi_server.dialogue.stream.api.event.TokenEventDto apiEvent) {
        if (apiEvent == null) {
            return null;
        }
        return new TokenEventDto(
                apiEvent.characterConfig().characterId(), 
                apiEvent.token(), 
                apiEvent.id()
        );
    }

    public CharacterCompleteEventDto toCharacterCompleteEventDto(com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterCompleteEventDto apiEvent) {
        if (apiEvent == null) {
            return null;
        }
        return new CharacterCompleteEventDto(
                apiEvent.characterId(), 
                apiEvent.tokenCount(), 
                apiEvent.messageSequenceNumber(),
                apiEvent.id()
        );
    }

    public DialogueCompleteEventDto toDialogueCompleteEventDto(com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueCompleteEventDto apiEvent) {
        if (apiEvent == null) {
            return null;
        }
        return new DialogueCompleteEventDto(
                apiEvent.status(), 
                apiEvent.turnCount(), 
                apiEvent.id()
        );
    }

    public ErrorEventDto toErrorEventDto(com.github.vvojtas.dailogi_server.dialogue.stream.api.event.ErrorEventDto apiEvent) {
        if (apiEvent == null) {
            return null;
        }
        return new ErrorEventDto(
                apiEvent.message(), 
                apiEvent.recoverable(), 
                apiEvent.id()
        );
    }
} 