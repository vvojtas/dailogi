package com.github.vvojtas.dailogi_server.model.dialogue.response.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;

import java.util.List;

/**
 * Event DTO sent at the start of a dialogue stream
 */
public record DialogueStartEventDto(
    @JsonProperty("dialogue_id") Long dialogueId,
    @JsonProperty("character_configs") List<CharacterConfigDTO> characterConfigs,
    @JsonProperty("turn_count") int turnCount
) {} 