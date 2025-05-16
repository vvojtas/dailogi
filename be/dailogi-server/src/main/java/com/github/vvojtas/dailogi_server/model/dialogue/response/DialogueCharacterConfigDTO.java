package com.github.vvojtas.dailogi_server.model.dialogue.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for DialogueCharacterConfig entity
 */
@Schema(description = "Configuration of a character within a dialogue")
public record DialogueCharacterConfigDTO(
    @Schema(description = "Summary of the character participating", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("character") CharacterDTO character,

    @Schema(description = "The language model used by this character in the dialogue", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("llm") LLMDTO llm
) {
    /**
     * Converts this DTO to a CharacterConfigDTO for use in dialogue generation
     * 
     * @return A CharacterConfigDTO containing the character and LLM IDs
     */
    public CharacterConfigDTO toCharacterConfigDTO() {
        return new CharacterConfigDTO(
            character.id(),
            llm.id()
        );
    }
}
