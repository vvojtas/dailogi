package com.github.vvojtas.dailogi_server.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for OpenRouter API key encryption
 */
@Component
@ConfigurationProperties(prefix = "openrouter.encryption")
@Getter
@Setter
public class OpenRouterEncryptionProperties {
    /**
     * The encryption key used for API key encryption/decryption
     */
    private String key;
    
    /**
     * GCM initialization vector length in bytes (default: 12 bytes, which is 96 bits)
     */
    private int ivLength = 12;
    
    /**
     * GCM tag length in bits (default: 128 bits)
     */
    private int tagLength = 128;
} 