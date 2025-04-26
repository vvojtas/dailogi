package com.github.vvojtas.dailogi_server.model.character.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for character avatar operations
 */
@Schema(description = "Response confirming character avatar status")
public record CharacterAvatarResponseDTO(
    @Schema(description = "Unique identifier for the character", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id,

    @Schema(description = "Indicates if the character now has an avatar", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("has_avatar") boolean hasAvatar,

    @Schema(description = "New URL of the character's avatar image (null if avatar was removed or upload failed)", example = "/api/characters/1/avatar", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("avatar_url") String avatarUrl
) {} 
