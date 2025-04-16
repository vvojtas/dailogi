package com.github.vvojtas.dailogi_server.model.dialogue.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for dialogue generation requests
 */
public record GenerateDialogueResponseDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("status") String status
) {} 
