package com.github.vvojtas.dailogi_server.apikey.api;

/**
 * Command to set an API key for the current user
 */
public record SetApiKeyCommand(
    String apiKey
) {} 