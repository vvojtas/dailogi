package com.github.vvojtas.dailogi_server.dialogue.stream.api;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.*;

/**
 * Defines handlers for events occurring during dialogue generation.
 * Implementations will typically handle sending these events over a specific protocol (like SSE).
 */
public interface DialogueEventHandler {

    /**
     * Handles the start of the dialogue generation process.
     * @param event The dialogue start event data.
     */
    void onDialogueStart(DialogueStartEventDto event);

    /**
     * Handles the start of a character's turn.
     * @param event The character start event data.
     */
    void onCharacterStart(CharacterStartEventDto event);

    /**
     * Handles a generated token.
     * @param event The token event data.
     */
    void onToken(TokenEventDto event);

    /**
     * Handles the completion of a character's turn.
     * @param event The character complete event data.
     */
    void onCharacterComplete(CharacterCompleteEventDto event);

    /**
     * Handles the successful completion of the entire dialogue generation.
     * @param event The dialogue complete event data.
     */
    void onDialogueComplete(DialogueCompleteEventDto event);

    /**
     * Handles an error that occurred during dialogue generation.
     * @param dialogueId The ID of the dialogue where the error occurred.
     * @param exception The exception that was thrown.
     */
    void onError(long dialogueId, Exception exception);

} 