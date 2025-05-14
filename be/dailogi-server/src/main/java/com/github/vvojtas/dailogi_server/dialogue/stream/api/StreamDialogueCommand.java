package com.github.vvojtas.dailogi_server.dialogue.stream.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * Command for streaming a dialogue with real-time token generation
 */
public record StreamDialogueCommand(
    @NotBlank(message = "Scene description is required")
    @Size(max = 500, message = "Scene description must not exceed 500 characters")
    String sceneDescription,
    
    @NotEmpty(message = "Character configurations are required")
    @Size(min = 2, max = 3, message = "Must include 2-3 characters")
    @Valid
    List<CharacterConfigDto> characterConfigs,
    
    @Min(value = 1, message = "Length must be at least 1")
    @Max(value = 50, message = "Length must not exceed 50")
    Integer length
) {} 