package com.github.vvojtas.dailogi_server.dialogue.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;

/**
 * Command for creating a new dialogue
 */
public record CreateDialogueCommand(
    @Size(max = 100, message = "Dialogue name must not exceed 100 characters")
    String name,
    
    @NotBlank(message = "Scene description is required")
    @Size(max = 500, message = "Scene description must not exceed 500 characters")
    String sceneDescription,
    
    @NotEmpty(message = "Character configurations are required")
    @Size(min = 2, max = 3, message = "Must include 2-3 characters")
    @Valid
    List<CharacterConfigDTO> characterConfigs,
    
    Boolean isGlobal
) {} 