package com.github.vvojtas.dailogi_server.generation.api;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;

import java.util.List;
import java.util.Map;

/**
 * Interface for building prompts for the OpenRouter API.
 */
public interface OpenRouterPromptBuilder {
    
    /**
     * Builds a list of chat messages for a dialogue based on the provided command and character information.
     *
     * @param command The command containing dialogue information
     * @param characters Map of character IDs to character DTOs
     * @param llms Map of LLM IDs to LLM DTOs
     * @return A list of chat messages for the OpenRouter API
     */
    List<ChatMessage> buildDialogueMessages(
            StreamDialogueCommand command,
            Map<Long, CharacterDTO> characters,
            Map<Long, LLMDTO> llms
    );
} 