package com.github.vvojtas.dailogi_server.model.dialogue.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command model for configuring a character in a dialogue
 */
public record DialogueCharacterConfigCommand(
    @JsonProperty("character_id") Long characterId,
    @JsonProperty("llm_id") Long llmId
) {} 
