package com.github.vvojtas.dailogi_server.model.dialogue.response.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event DTO sent for each token in the dialogue stream
 */
public record TokenEventDto(
    @JsonProperty("character_id") Long characterId,
    @JsonProperty("token") String token,
    @JsonProperty("id") String id
) {} 