package com.github.vvojtas.dailogi_server.model.apikey.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for sending OpenRouter API key
 */
@Schema(description = "Request for saving an OpenRouter API key")
public record SetApiKeyCommand(
    @Schema(description = "OpenRouter API key", example = "sk-or-v1-...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "API key is required")
    @JsonProperty("api_key") String apiKey
) {} 