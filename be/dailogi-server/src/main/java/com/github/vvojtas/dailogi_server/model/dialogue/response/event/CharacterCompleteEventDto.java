package com.github.vvojtas.dailogi_server.model.dialogue.response.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event DTO sent when a character's response is complete in the dialogue stream
 */
public record CharacterCompleteEventDto(
    @JsonProperty("character_id") Long characterId,
    @JsonProperty("token_count") int tokenCount,
    @JsonProperty("message_sequence_number") int messageSequenceNumber,
    @JsonProperty("id") String id
) {} 