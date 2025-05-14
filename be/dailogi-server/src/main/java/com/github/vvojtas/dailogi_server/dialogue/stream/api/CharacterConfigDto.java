package com.github.vvojtas.dailogi_server.dialogue.stream.api;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for character configuration in dialogue stream command
 */
public record CharacterConfigDto(
    @NotNull(message = "Character ID is required")
    Long characterId,
    
    @NotNull(message = "LLM ID is required")
    Long llmId
) {} 