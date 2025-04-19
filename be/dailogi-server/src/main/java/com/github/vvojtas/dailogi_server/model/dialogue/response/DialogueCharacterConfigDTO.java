package com.github.vvojtas.dailogi_server.model.dialogue.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vvojtas.dailogi_server.model.character.response.DialogueCharacterSummaryDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;

/**
 * DTO for DialogueCharacterConfig entity
 */
public record DialogueCharacterConfigDTO(
    @JsonProperty("character") DialogueCharacterSummaryDTO character,
    @JsonProperty("llm") LLMDTO llm
) {} 
