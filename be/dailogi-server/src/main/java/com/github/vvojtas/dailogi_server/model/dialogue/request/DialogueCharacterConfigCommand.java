package com.github.vvojtas.dailogi_server.model.dialogue.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Command model for configuring a character in a dialogue
 */
@Schema(description = "Configuration for a character participating in a dialogue")
public record DialogueCharacterConfigCommand(
    @Schema(description = "ID of the character to include in the dialogue", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Character ID is required")
    @JsonProperty("character_id") Long characterId,

    @Schema(description = "ID of the language model to use for this character", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Language model ID is required")
    @JsonProperty("llm_id") Long llmId
) {} 
