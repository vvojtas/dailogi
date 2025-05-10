package com.github.vvojtas.dailogi_server.model.apikey.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request object for setting an OpenRouter API key
 */
@Schema(description = "Request to set an OpenRouter API key")
public record ApiKeyRequest(
    @Schema(description = "The OpenRouter API key to set", 
            example = "sk-or-v1-...", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("api_key") 
    @NotBlank(message = "API key cannot be empty")
    String apiKey
) {} 