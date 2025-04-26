package com.github.vvojtas.dailogi_server.model.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic message response DTO
 */
@Schema(description = "A generic response containing a message")
public record MessageResponseDTO(
    @Schema(description = "The response message", example = "Operation successful", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("message") String message
) {} 
