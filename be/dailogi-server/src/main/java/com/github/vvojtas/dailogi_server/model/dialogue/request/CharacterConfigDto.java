package com.github.vvojtas.dailogi_server.model.dialogue.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for character configuration in dialogue stream
 */
@Schema(description = "Configuration for a character in a dialogue stream")
public record CharacterConfigDTO(
    @Schema(description = "ID of the character", 
           example = "1", 
           requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Character ID is required")
    @JsonProperty("character_id") Long characterId,
    
    @Schema(description = "ID of the language model to use for the character", 
           example = "1", 
           requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "LLM ID is required")
    @JsonProperty("llm_id") Long llmId
) {} 