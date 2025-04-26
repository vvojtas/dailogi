package com.github.vvojtas.dailogi_server.model.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing JWT authentication details")
public record JwtResponseDTO(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzUxMiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("access_token") String accessToken,

    @Schema(description = "Type of the token", example = "Bearer", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("token_type") String tokenType,

    @Schema(description = "Token expiration time in seconds", example = "3600", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("expires_in") Long expiresIn,

    @Schema(description = "User details associated with the token", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("user") UserDto user
) {} 