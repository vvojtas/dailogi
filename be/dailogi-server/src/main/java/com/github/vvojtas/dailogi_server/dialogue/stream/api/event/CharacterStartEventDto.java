package com.github.vvojtas.dailogi_server.dialogue.stream.api.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;

/**
 * Event sent when a character starts its turn in the dialogue generation.
 */
@Data
@RequiredArgsConstructor
public class CharacterStartEventDto {
    /**
     * The configuration of the character starting its turn (includes characterId and llmId).
     */
    private final CharacterConfigDTO characterConfig;

    /**
     * Unique ID for this event, useful for idempotency or client-side tracking.
     */
    private final String eventId;
} 