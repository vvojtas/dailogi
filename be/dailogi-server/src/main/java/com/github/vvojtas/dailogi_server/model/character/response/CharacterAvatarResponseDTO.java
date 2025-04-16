package com.github.vvojtas.dailogi_server.model.character.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for character avatar operations
 */
public record CharacterAvatarResponseDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("has_avatar") boolean hasAvatar,
    @JsonProperty("avatar_url") String avatarUrl
) {} 
