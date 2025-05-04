package com.github.vvojtas.dailogi_server.character.api;

public record AvatarData(
    byte[] data,
    String contentType
) {} 