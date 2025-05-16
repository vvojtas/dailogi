package com.github.vvojtas.dailogi_server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.Getter;

/**
 * Exception thrown when an API key is required but not provided or is invalid.
 * Results in HTTP 402 Payment Required response.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class NoApiKeyException extends RuntimeException {
    
    @Getter
    private final String operation;
    
    /**
     * Constructs a new NoApiKeyException
     * 
     * @param operation The operation that requires an API key
     * @param message Detailed message
     */
    public NoApiKeyException(String operation, String message) {
        super(message);
        this.operation = operation;
    }
    
    /**
     * Constructs a new NoApiKeyException with a standard message
     * 
     * @param operation The operation that requires an API key
     */
    public NoApiKeyException(String operation) {
        this(operation, "Valid API key required for operation: " + operation);
    }
} 