package com.github.vvojtas.dailogi_server.model.character.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

/**
 * DTO for Character entity
 */
@Schema(description = "Data Transfer Object for a Character")
public record CharacterDTO(
    @Schema(description = "Unique identifier for the character", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id,

    @Schema(description = "Name of the character", example = "Sherlock Holmes", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name") String name,

    @Schema(description = "Brief character description", example = "A brilliant detective...", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("short_description") String shortDescription,

    @Schema(description = "Detailed character description", example = "Sherlock Holmes is a fictional detective...", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("description") String description,

    @Schema(description = "Indicates if the character has an avatar", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("has_avatar") boolean hasAvatar,

    @Schema(description = "URL of the character's avatar image (null if no avatar)", example = "/api/characters/1/avatar", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("avatar_url") String avatarUrl,

    @Schema(description = "Indicates if the character is globally available", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("is_global") boolean isGlobal,

    @Schema(description = "Default language model ID for this character (null if not set)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("default_llm_id") Long defaultLlmId,

    @Schema(description = "Timestamp when the character was created", example = "2023-10-26T10:15:30+00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("created_at") OffsetDateTime createdAt,

    @Schema(description = "Timestamp when the character was last updated (null if never updated)", example = "2023-10-27T11:00:00+00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("updated_at") OffsetDateTime updatedAt
) {} 
