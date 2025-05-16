package com.github.vvojtas.dailogi_server.model.dialogue.mapper;

import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.DialogueCharacterConfig;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterMapper;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueCharacterConfigDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueDTO;
import com.github.vvojtas.dailogi_server.model.llm.mapper.LLMMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Dialogue entities
 */
@Component
@RequiredArgsConstructor
public class DialogueMapper {
    
    private final CharacterMapper characterMapper;
    private final LLMMapper llmMapper;
    
    /**
     * Maps a Dialogue entity to a DialogueDTO
     *
     * @param dialogue The dialogue entity to map
     * @param characterConfigs The character configurations for this dialogue
     * @return A DialogueDTO representing the dialogue
     */
    public DialogueDTO toDTO(Dialogue dialogue, List<DialogueCharacterConfig> characterConfigs) {
        if (dialogue == null) {
            return null;
        }
        
        List<DialogueCharacterConfigDTO> configDTOs = characterConfigs.stream()
            .map(this::toDialogueCharacterConfigDTO)
            .collect(Collectors.toList());
        
        return new DialogueDTO(
            dialogue.getId(),
            dialogue.getName(),
            dialogue.getSceneDescription(),
            dialogue.getStatus().name(),
            dialogue.getCreatedAt(),
            dialogue.getUpdatedAt(),
            configDTOs,
            new ArrayList<>() // Empty messages list, as we haven't loaded them
        );
    }
    
    
    /**
     * Maps a DialogueCharacterConfig entity to a DialogueCharacterConfigDTO
     *
     * @param config The config entity to map
     * @return A DialogueCharacterConfigDTO
     */
    private DialogueCharacterConfigDTO toDialogueCharacterConfigDTO(DialogueCharacterConfig config) {
        if (config == null) {
            return null;
        }
        
        return new DialogueCharacterConfigDTO(
            characterMapper.toDTO(config.getCharacter()),
            llmMapper.toDTO(config.getLlm())
        );
    }
} 