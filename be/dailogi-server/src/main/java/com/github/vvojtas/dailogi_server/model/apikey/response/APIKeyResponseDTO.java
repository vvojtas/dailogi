package com.github.vvojtas.dailogi_server.model.apikey.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for API key operation responses
 */
@Schema(description = "Response for API key operations")
public record ApiKeyResponseDTO(
    @Schema(description = "Indicates if the user has an API key set", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("has_api_key") boolean hasApiKey
) {} 
