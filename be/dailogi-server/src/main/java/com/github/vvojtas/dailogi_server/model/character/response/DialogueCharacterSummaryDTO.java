package com.github.vvojtas.dailogi_server.model.character.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Brief character information for dialogue lists
 */
@Schema(description = "Summary information for a character involved in a dialogue")
public record DialogueCharacterSummaryDTO(
    @Schema(description = "Unique identifier for the character", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id,

    @Schema(description = "Name of the character", example = "Sherlock Holmes", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name") String name,

    @Schema(description = "Indicates if the character has an avatar", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("has_avatar") boolean hasAvatar,

    @Schema(description = "URL of the character's avatar image (null if no avatar)", example = "/api/characters/1/avatar", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("avatar_url") String avatarUrl
) {} 
