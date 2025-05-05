package com.github.vvojtas.dailogi_server.controller;

import com.github.vvojtas.dailogi_server.llm.api.GetLLMsQuery;
import com.github.vvojtas.dailogi_server.llm.application.LLMQueryService;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/llms")
@RequiredArgsConstructor
@Tag(name = "LLM", description = "Language Model management endpoints")
public class LLMController {
    private final LLMQueryService llmQueryService;

    @Operation(
        summary = "Get all available LLMs",
        description = "Retrieves a list of all available Language Models in the system"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved list of LLMs",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = LLMDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(hidden = true))
    )
    @GetMapping
    public ResponseEntity<List<LLMDTO>> getLLMs() {
        return ResponseEntity.ok(llmQueryService.getLLMs(new GetLLMsQuery()));
    }
} 