package com.github.vvojtas.dailogi_server.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for OpenRouter API integration
 */
@ConfigurationProperties(prefix = "openrouter.api")
@Getter
@Setter
public class OpenRouterProperties {
    
    /**
     * Base URL for the OpenRouter API
     */
    private String baseUrl = "https://openrouter.ai/api/v1";
    
    /**
     * Connect timeout for the OpenRouter API client
     */
    private Duration connectTimeout = Duration.ofSeconds(30);
    
    /**
     * Read timeout for the OpenRouter API client
     */
    private Duration readTimeout = Duration.ofSeconds(120);
    
    /**
     * Flag to enable mocking OpenRouter API responses
     */
    private boolean mockEnabled = false;

} 