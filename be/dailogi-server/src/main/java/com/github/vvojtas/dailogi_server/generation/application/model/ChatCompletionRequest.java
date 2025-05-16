package com.github.vvojtas.dailogi_server.generation.application.model;

import java.util.List;

import com.github.vvojtas.dailogi_server.generation.api.ChatMessage;

/**
 * Request body for chat completions API
 */
public record ChatCompletionRequest(
        String model,
        List<ChatMessage> messages,
        boolean stream
) {}