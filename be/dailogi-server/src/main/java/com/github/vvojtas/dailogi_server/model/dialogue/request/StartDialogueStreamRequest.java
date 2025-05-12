package com.github.vvojtas.dailogi_server.model.dialogue.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * Request model for starting a dialogue stream with SSE
 */
@Schema(description = "Request to start a dialogue stream with real-time token generation")
public record StartDialogueStreamRequest(
    @Schema(description = "Description of the scene or context for the dialogue", 
           example = "A heated debate about artificial intelligence in a coffee shop", 
           requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Scene description is required")
    @Size(max = 500, message = "Scene description must not exceed 500 characters")
    @JsonProperty("scene_description") String sceneDescription,

    @Schema(description = "List of characters and their configurations for the dialogue (2-3 characters required)", 
           minLength = 2, 
           maxLength = 3, 
           requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Character configurations are required")
    @Size(min = 2, max = 3, message = "Must include 2-3 characters")
    @Valid
    @JsonProperty("character_configs") List<CharacterConfigDto> characterConfigs,
    
    @Schema(description = "Number of turns in the dialogue (1-50)",
           example = "5",
           minimum = "1",
           maximum = "50",
           defaultValue = "5",
           requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "Length must be at least 1")
    @Max(value = 50, message = "Length must not exceed 50")
    @JsonProperty("length") Integer length
) {} 