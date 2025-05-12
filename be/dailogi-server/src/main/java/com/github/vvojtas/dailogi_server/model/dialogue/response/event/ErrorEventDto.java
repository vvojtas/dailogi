package com.github.vvojtas.dailogi_server.model.dialogue.response.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event DTO sent when an error occurs during the dialogue stream
 */
public record ErrorEventDto(
    @JsonProperty("message") String message,
    @JsonProperty("recoverable") boolean recoverable,
    @JsonProperty("id") String id
) {} 