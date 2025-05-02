package com.github.vvojtas.dailogi_server.exception;

import lombok.Getter;

/**
 * Exception thrown when a user attempts to create more characters than allowed.
 */
@Getter
public class CharacterLimitExceededException extends RuntimeException {

    private final int limit;

    public CharacterLimitExceededException(int limit) {
        super(String.format("Cannot create more characters. Maximum limit of %d characters reached.", limit));
        this.limit = limit;
    }
} 