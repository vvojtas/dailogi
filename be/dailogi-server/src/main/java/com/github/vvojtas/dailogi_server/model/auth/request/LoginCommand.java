package com.github.vvojtas.dailogi_server.model.auth.request;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
    @NotBlank(message = "Username is required")
    String name,
    
    @NotBlank(message = "Password is required")
    String password
) {} 