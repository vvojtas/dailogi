package com.github.vvojtas.dailogi_server.model.auth.response;

public record JwtResponseDTO(
    String accessToken,
    String tokenType,
    Long expiresIn,
    UserDto user
) {} 