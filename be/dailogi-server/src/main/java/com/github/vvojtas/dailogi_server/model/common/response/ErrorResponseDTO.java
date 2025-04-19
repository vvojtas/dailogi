package com.github.vvojtas.dailogi_server.model.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Standard error response format")
public record ErrorResponseDTO(
    @Schema(description = "Human-readable error message", example = "Validation failed")
    String message,
    
    @Schema(description = "Error code for programmatic handling", example = "VALIDATION_ERROR")
    String code,
    
    @Schema(description = "Additional error details, specific to the error type", 
           example = "{\"page\": \"must be greater than or equal to 0\"}")
    Map<String, Object> details,
    
    @Schema(description = "Timestamp when the error occurred", 
           example = "2024-02-20T15:30:45.123Z")
    OffsetDateTime timestamp
) {} 