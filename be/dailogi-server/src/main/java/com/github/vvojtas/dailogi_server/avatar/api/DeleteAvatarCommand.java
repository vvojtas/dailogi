package com.github.vvojtas.dailogi_server.avatar.api;

import org.springframework.security.core.Authentication;

public record DeleteAvatarCommand(
    Long characterId,
    Authentication authentication
) {} 