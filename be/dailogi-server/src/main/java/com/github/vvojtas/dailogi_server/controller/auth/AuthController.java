package com.github.vvojtas.dailogi_server.controller.auth;

import com.github.vvojtas.dailogi_server.model.auth.request.LoginCommand;
import com.github.vvojtas.dailogi_server.model.auth.request.RegisterCommand;
import com.github.vvojtas.dailogi_server.model.auth.response.JwtResponseDTO;
import com.github.vvojtas.dailogi_server.model.auth.response.UserDto;
import com.github.vvojtas.dailogi_server.model.common.response.ErrorResponseDTO;
import com.github.vvojtas.dailogi_server.service.auth.AuthService;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management API")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User successfully registered",
                content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Username already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterCommand request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Authenticate user and get JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful",
                content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid credentials",
                content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginCommand request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(
        summary = "Get current user information",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current user information retrieved",
                content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(currentUserService.getCurrentUserDTO());
    }
} 