package com.github.vvojtas.dailogi_server.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command for user login")
public record LoginCommand(
    @Schema(description = "Username", example = "john.doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    String name,
    
    @Schema(description = "Password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    String password
) {} 