package com.github.vvojtas.dailogi_server.model.dialogue.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vvojtas.dailogi_server.model.character.response.DialogueCharacterSummaryDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for DialogueCharacterConfig entity
 */
@Schema(description = "Configuration of a character within a dialogue")
public record DialogueCharacterConfigDTO(
    @Schema(description = "Summary of the character participating", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("character") DialogueCharacterSummaryDTO character,

    @Schema(description = "The language model used by this character in the dialogue", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("llm") LLMDTO llm
) {} 
