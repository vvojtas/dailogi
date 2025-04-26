package com.github.vvojtas.dailogi_server.model.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Command for user registration")
public record RegisterCommand(
    @Schema(description = "Username", example = "new_user", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String name,
    
    @Schema(description = "Password", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    @Schema(description = "Password confirmation", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password confirmation is required")
    String passwordConfirmation
) {} 