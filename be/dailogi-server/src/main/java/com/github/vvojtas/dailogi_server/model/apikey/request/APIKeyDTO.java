package com.github.vvojtas.dailogi_server.model.apikey.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for sending OpenRouter API key
 */
public record APIKeyDTO(
    @JsonProperty("api_key") String apiKey
) {} 
