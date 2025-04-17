package com.github.vvojtas.dailogi_server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.Getter;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    @Getter
    private final String type;

    public ResourceNotFoundException(String type, String message) {
        super(message);
        this.type = type;
    }
} 