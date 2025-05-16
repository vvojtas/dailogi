package com.github.vvojtas.dailogi_server.exception;

import lombok.Getter;

/**
 * Exception thrown when a user attempts to delete a character that is being used in dialogues.
 */
@Getter
public class CharacterInUseException extends RuntimeException {

    private final Long characterId;

    public CharacterInUseException(Long characterId) {
        super(String.format("Character with ID %d cannot be deleted because it is used in one or more dialogues.", characterId));
        this.characterId = characterId;
    }
} 