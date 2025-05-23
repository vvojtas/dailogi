package com.github.vvojtas.dailogi_server.controller;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.stream.application.DialogueStreamService;
import com.github.vvojtas.dailogi_server.model.common.response.ErrorResponseDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.request.StartDialogueStreamRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for streaming dialogue generation using Server-Sent Events
 */
@RestController
@RequestMapping("/api/dialogues")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dialogues", description = "Endpoints for dialogue generation and management")
public class DialogueStreamController {

    private final DialogueStreamService dialogueStreamService;

    @Operation(
        summary = "Stream dialogue generation",
        description = """
            Starts real-time dialogue generation between characters using Server-Sent Events (SSE).
            The client sends a scene description and character configurations, and the server
            responds with a stream of events, including tokens generated by the characters in real-time.
            Requires authentication with a valid API key.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "SSE stream started successfully",
        content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - user not authenticated",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "402",
        description = "Payment required - no valid API key available",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - resource limit reached or invalid state",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public SseEmitter streamDialogue(
            @Valid @RequestBody StartDialogueStreamRequest request,
            Authentication authentication) {
        
        log.debug("Received request to stream dialogue with {} characters, length={}", 
                request.characterConfigs().size(), request.length());
        
        // Convert request to command
        StreamDialogueCommand command = new StreamDialogueCommand(
            null,
                request.sceneDescription(),
                request.characterConfigs(),
                request.length()
        );
        
        // Start dialogue streaming - this should return quickly as the actual generation is async
        var emitter = dialogueStreamService.streamDialogue(command, authentication);
        log.info("Dialogue stream started!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return emitter;
    }
} 