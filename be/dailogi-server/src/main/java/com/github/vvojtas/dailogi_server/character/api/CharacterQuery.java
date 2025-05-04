package com.github.vvojtas.dailogi_server.character.api;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public record CharacterQuery(
    boolean includeGlobal,
    Pageable pageable,
    Authentication authentication
) {} 