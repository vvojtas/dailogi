package com.github.vvojtas.dailogi_server.model.auth.response;

import java.time.OffsetDateTime;

public record UserDto(
    Long id,
    String name,
    OffsetDateTime createdAt
) {} 