package com.github.vvojtas.dailogi_server.avatar.api;

import org.springframework.security.core.Authentication;

public record GetAvatarQuery(
    Long characterId,
    Authentication authentication
) {} 