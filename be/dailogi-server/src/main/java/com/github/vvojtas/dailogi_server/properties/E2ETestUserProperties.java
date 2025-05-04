package com.github.vvojtas.dailogi_server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for E2E test user
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "dailogi.e2e-test.user")
@Validated
public class E2ETestUserProperties {
    
    /**
     * Username for the E2E test user
     */
    private String name;
    
    /**
     * Password for the E2E test user
     */
    private String password;
} 