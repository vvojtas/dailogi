package com.github.vvojtas.dailogi_server.service.util;

import lombok.Getter;

/**
 * Represents the result of an encryption operation
 */
@Getter
public class EncryptionResult {
    private final String cipherText;
    private final byte[] nonce;
    
    public EncryptionResult(String cipherText, byte[] nonce) {
        this.cipherText = cipherText;
        this.nonce = nonce;
    }
}