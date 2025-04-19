package com.github.vvojtas.dailogi_server.model.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic message response DTO
 */
public record MessageResponseDTO(
    @JsonProperty("message") String message
) {} 
