package com.github.vvojtas.dailogi_server.model.dialogue.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for DialogueMessage entity
 */
public record DialogueMessageDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("turn_number") int turnNumber,
    @JsonProperty("character_id") Long characterId,
    @JsonProperty("content") String content
) {} 
