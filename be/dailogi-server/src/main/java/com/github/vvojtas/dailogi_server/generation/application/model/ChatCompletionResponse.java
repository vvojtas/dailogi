package com.github.vvojtas.dailogi_server.generation.application.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response structure for chat token
 */
record ChatCompletionResponse(
        @JsonProperty("choices")
        List<Choice> choices
) {
    record Choice(
            @JsonProperty("delta")
            Delta delta
    ) {
        record Delta(
                @JsonProperty("content")
                String content
        ) {}
    }
}