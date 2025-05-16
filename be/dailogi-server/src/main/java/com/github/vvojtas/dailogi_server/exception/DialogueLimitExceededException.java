package com.github.vvojtas.dailogi_server.exception;

import lombok.Getter;

/**
 * Exception thrown when a user attempts to create more dialogues than allowed.
 */
@Getter
public class DialogueLimitExceededException extends RuntimeException {

    private final int limit;

    public DialogueLimitExceededException(int limit) {
        super(String.format("Cannot create more dialogues. Maximum limit of %d dialogues reached.", limit));
        this.limit = limit;
    }
} 