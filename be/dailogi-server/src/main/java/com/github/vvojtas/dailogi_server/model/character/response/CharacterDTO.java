package com.github.vvojtas.dailogi_server.model.character.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * DTO for Character entity
 */
public record CharacterDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("short_description") String shortDescription,
    @JsonProperty("description") String description,
    @JsonProperty("has_avatar") boolean hasAvatar,
    @JsonProperty("avatar_url") String avatarUrl,
    @JsonProperty("is_global") boolean isGlobal,
    @JsonProperty("default_llm_id") Long defaultLlmId,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    @JsonProperty("updated_at") OffsetDateTime updatedAt
) {} 
