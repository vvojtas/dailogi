package com.github.vvojtas.dailogi_server.dialogue.stream.api.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.CharacterConfigDto;

/**
 * Event DTO sent for each token in the dialogue stream
 */
public record TokenEventDto(
    @JsonProperty("character_config") CharacterConfigDto characterConfig,
    @JsonProperty("token") String token,
    @JsonProperty("id") String id
) {} 