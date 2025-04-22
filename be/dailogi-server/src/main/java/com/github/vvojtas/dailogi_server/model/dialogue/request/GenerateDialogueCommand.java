package com.github.vvojtas.dailogi_server.model.dialogue.request; 
 
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;  
 
/** 
 * Command model for generating a dialogue 
 */ 
@Schema(description = "Command for generating a new dialogue between characters")
public record GenerateDialogueCommand( 
    @Schema(description = "Description of the scene or context for the dialogue", example = "A heated debate about artificial intelligence in a coffee shop", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Scene description is required")
    @Size(max = 1000, message = "Scene description must not exceed 1000 characters")
    @JsonProperty("scene_description") String sceneDescription,

    @Schema(description = "List of characters and their configurations for the dialogue (2-3 characters required)", minLength = 2, maxLength = 3, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Character configurations are required")
    @Size(min = 2, max = 3, message = "Must include 2-3 characters")
    @JsonProperty("character_configs") List<DialogueCharacterConfigCommand> characterConfigs 
) {} 
