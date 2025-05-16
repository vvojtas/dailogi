package com.github.vvojtas.dailogi_server.model.dialogue.response.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;

/**
 * Event sent when a character starts its turn in the dialogue generation.
 */
@Data
@RequiredArgsConstructor
public class CharacterStartEventDto {
    /**
     * The configuration of the character starting its turn (includes characterId and llmId).
     */
    @JsonProperty("character_config") private final CharacterConfigDTO characterConfig;

    /**
     * Unique ID for this event, useful for idempotency or client-side tracking.
     */
    @JsonProperty("id") private final String id;
} 