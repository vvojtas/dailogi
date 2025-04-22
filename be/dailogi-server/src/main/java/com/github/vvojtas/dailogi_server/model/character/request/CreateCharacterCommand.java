package com.github.vvojtas.dailogi_server.model.character.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command model for creating a new character
 */
@Schema(description = "Command for creating a new character")
public record CreateCharacterCommand(
    @Schema(description = "Character name", example = "Sherlock Holmes", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @JsonProperty(value = "name") String name,

    @Schema(description = "Brief character description", example = "A brilliant detective with exceptional deductive reasoning", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Short description is required")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    @JsonProperty("short_description") String shortDescription,

    @Schema(description = "Detailed character description", example = "Sherlock Holmes is a fictional detective created by British author Sir Arthur Conan Doyle...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @JsonProperty("description") String description,

    @Schema(description = "Default language model ID to use for this character", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("default_llm_id") Long defaultLlmId
) {} 
