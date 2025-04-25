package com.github.vvojtas.dailogi_server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
    USER("ROLE_USER"),
    SPECIAL("ROLE_SPECIAL");

    private final String name;
} 