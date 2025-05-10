package com.github.vvojtas.dailogi_server.model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Data Transfer Object for User information")
public record UserDto(
    @Schema(description = "Unique identifier for the user", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id,

    @Schema(description = "Username", example = "john.doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name") String name,

    @Schema(description = "Timestamp when the user was created", example = "2023-10-26T10:15:30+00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("created_at") OffsetDateTime createdAt,
    
    @Schema(description = "Indicates if the user has an OpenRouter API key set", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("has_api_key") boolean hasApiKey
) {} 