package com.github.vvojtas.dailogi_server.model.avatar;

/**
 * Represents the data and format type of an avatar.
 */
public record AvatarData(
    byte[] data,
    String formatType
) {} 