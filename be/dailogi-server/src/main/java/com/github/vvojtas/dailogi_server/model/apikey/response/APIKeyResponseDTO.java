package com.github.vvojtas.dailogi_server.model.apikey.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for API key operations
 */
@Schema(description = "Response related to user's API key status")
public record APIKeyResponseDTO(
    @Schema(description = "Result message of the operation", example = "API key set successfully", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("message") String message,

    @Schema(description = "Indicates if the user currently has an API key set", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("has_api_key") boolean hasApiKey
) {} 
