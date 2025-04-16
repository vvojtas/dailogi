package com.github.vvojtas.dailogi_server.model.apikey.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for API key operations
 */
public record APIKeyResponseDTO(
    @JsonProperty("message") String message,
    @JsonProperty("has_api_key") boolean hasApiKey
) {} 
