package com.github.vvojtas.dailogi_server.model.character.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Brief character information for dialogue lists
 */
public record DialogueCharacterSummaryDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("has_avatar") boolean hasAvatar,
    @JsonProperty("avatar_url") String avatarUrl
) {} 
