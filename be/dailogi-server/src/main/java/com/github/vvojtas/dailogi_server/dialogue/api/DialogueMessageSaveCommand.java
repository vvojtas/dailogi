package com.github.vvojtas.dailogi_server.dialogue.api;

/**
 * Command for saving a dialogue message.
 */
public record DialogueMessageSaveCommand(
    Long characterId,
    String content,
    Integer messageSequenceNumber
) {} 