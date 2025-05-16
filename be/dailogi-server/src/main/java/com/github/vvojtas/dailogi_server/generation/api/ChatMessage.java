package com.github.vvojtas.dailogi_server.generation.api;

/**
 * Represents a message in a chat conversation using the OpenRouter API structure.
 * @param role The role of the message sender (system, user, assistant)
 * @param content The content of the message
 */
public record ChatMessage(String role, String content) {
    // Common roles
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
} 