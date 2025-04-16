package com.github.vvojtas.dailogi_server.model.llm.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for LLM entity
 */
public record LLMDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("openrouter_identifier") String openrouterIdentifier
) {} 
