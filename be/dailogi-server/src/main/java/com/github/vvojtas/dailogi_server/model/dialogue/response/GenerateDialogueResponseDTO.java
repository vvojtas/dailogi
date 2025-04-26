package com.github.vvojtas.dailogi_server.model.dialogue.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for dialogue generation requests
 */
@Schema(description = "Response confirming the initiation of a dialogue generation process")
public record GenerateDialogueResponseDTO(
    @Schema(description = "Unique identifier assigned to the newly created dialogue", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id,

    @Schema(description = "Initial status of the dialogue (e.g., PENDING)", example = "PENDING", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("status") String status
) {} 
