package com.github.vvojtas.dailogi_server.service.util;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for key management operations.
 */
public class KeyUtils {

    /**
     * Decodes a key string that could be either Base64 encoded or raw text.
     * 
     * @param key the key string to decode
     * @return the decoded key as a byte array
     * @throws IllegalStateException if the key is not configured or has invalid format
     */
    public static byte[] decodeKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalStateException("Key not configured");
        }
        
        try {
            // If the key is Base64 encoded, decode it
            if (key.matches("^[A-Za-z0-9+/=]+$")) {
                return Base64.getDecoder().decode(key);
            }
            
            // Otherwise, use it as a raw byte array
            return key.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid key format", e);
        }
    }
} 