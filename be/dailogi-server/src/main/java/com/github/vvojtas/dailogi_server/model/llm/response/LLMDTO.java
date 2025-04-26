package com.github.vvojtas.dailogi_server.model.llm.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for LLM entity
 */
@Schema(description = "Data Transfer Object for a Language Learning Model (LLM)")
public record LLMDTO(
    @Schema(description = "Unique identifier for the LLM", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id,

    @Schema(description = "Name of the LLM", example = "GPT-4", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name") String name,

    @Schema(description = "Identifier used by OpenRouter for this LLM", example = "openai/gpt-4", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("openrouter_identifier") String openrouterIdentifier
) {} 
