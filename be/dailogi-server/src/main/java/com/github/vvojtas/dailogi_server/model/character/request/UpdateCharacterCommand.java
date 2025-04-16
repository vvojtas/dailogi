package com.github.vvojtas.dailogi_server.model.character.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command model for updating an existing character
 */
public record UpdateCharacterCommand(
    @JsonProperty("name") String name,
    @JsonProperty("short_description") String shortDescription,
    @JsonProperty("description") String description,
    @JsonProperty("default_llm_id") Long defaultLlmId
) {} 
