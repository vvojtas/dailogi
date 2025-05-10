package com.github.vvojtas.dailogi_server.exception;

/**
 * Exception thrown when an encryption or decryption operation fails.
 */
public class CryptoException extends RuntimeException {

    /**
     * Creates a new CryptoException with the specified message.
     *
     * @param message the detail message
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * Creates a new CryptoException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
} 