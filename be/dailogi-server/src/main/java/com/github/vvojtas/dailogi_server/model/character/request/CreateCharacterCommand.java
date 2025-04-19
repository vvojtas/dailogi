package com.github.vvojtas.dailogi_server.model.character.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command model for creating a new character
 */
public record CreateCharacterCommand(
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @JsonProperty("name") String name,

    @NotBlank(message = "Short description is required")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    @JsonProperty("short_description") String shortDescription,

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @JsonProperty("description") String description,

    @JsonProperty("default_llm_id") Long defaultLlmId
) {} 
