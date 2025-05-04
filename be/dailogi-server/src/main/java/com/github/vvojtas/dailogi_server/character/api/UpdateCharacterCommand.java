package com.github.vvojtas.dailogi_server.character.api;

import com.github.vvojtas.dailogi_server.model.character.request.AvatarRequest;

public record UpdateCharacterCommand(
    Long id,
    String name,
    String shortDescription,
    String description,
    Long defaultLlmId,
    AvatarRequest avatar
) {} 