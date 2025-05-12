package com.github.vvojtas.dailogi_server.model.dialogue.response.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event DTO sent when the entire dialogue stream is complete
 */
public record DialogueCompleteEventDto(
    @JsonProperty("status") String status,
    @JsonProperty("turn_count") int turnCount,
    @JsonProperty("id") String id
) {} 