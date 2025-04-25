package com.github.vvojtas.dailogi_server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for user-specific limits
 */
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "dailogi.user.limits")
@Validated
public class UserLimitProperties {
    
    /**
     * Maximum number of characters a user can create
     */
    @Min(1)
    private int maxCharactersPerUser = 50;
} 