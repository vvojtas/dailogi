package com.github.vvojtas.dailogi_server.model.dialogue.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for DialogueMessage entity
 */
@Schema(description = "A single message within a dialogue")
public record DialogueMessageDTO(
    @Schema(description = "Unique identifier for the message", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id,

    @Schema(description = "Sequential turn number within the dialogue", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("turn_number") int turnNumber,

    @Schema(description = "ID of the character who spoke this message", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("character_id") Long characterId,

    @Schema(description = "Content of the message", example = "Elementary, my dear Watson.", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("content") String content
) {} 
