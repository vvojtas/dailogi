package com.github.vvojtas.dailogi_server.dialogue.application;

import com.github.vvojtas.dailogi_server.character.application.CharacterQueryService;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.DialogueCharacterConfig;
import com.github.vvojtas.dailogi_server.db.entity.DialogueCharacterConfigId;
import com.github.vvojtas.dailogi_server.db.entity.DialogueStatus;
import com.github.vvojtas.dailogi_server.db.repository.DialogueCharacterConfigRepository;
import com.github.vvojtas.dailogi_server.db.repository.DialogueRepository;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
import com.github.vvojtas.dailogi_server.dialogue.api.CreateDialogueCommand;
import com.github.vvojtas.dailogi_server.exception.DialogueLimitExceededException;
import com.github.vvojtas.dailogi_server.llm.application.LLMQueryService;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.mapper.DialogueMapper;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import com.github.vvojtas.dailogi_server.properties.UserLimitProperties;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for dialogue creation operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DialogueCommandService {
    
    private final CharacterQueryService characterQueryService;
    private final LLMQueryService llmQueryService;
    private final CurrentUserService currentUserService;
    private final DialogueRepository dialogueRepository;
    private final DialogueCharacterConfigRepository dialogueCharacterConfigRepository;
    private final UserLimitProperties userLimitProperties;
    private final DialogueMapper dialogueMapper;
    private final DialogueValidator validator;
    
    /**
     * Creates a new dialogue for the current user with the specified characters
     * 
     * @param command the command containing dialogue creation data
     * @return the created dialogue as DTO
     * @throws DialogueLimitExceededException if the user has reached their dialogue limit
     */
    @Transactional
    public DialogueDTO createDialogue(CreateDialogueCommand command) {
        AppUser currentUser = currentUserService.getCurrentAppUser();
        log.debug("Creating dialogue for user {}", currentUser.getId());
        
        // Validate command
        validator.validateForCreation(command, currentUser);
        
        // Check if the user has reached the dialogue limit
        long userDialogueCount = dialogueRepository.countByUser(currentUser);
        if (userDialogueCount >= userLimitProperties.getMaxDialoguesPerUser()) {
            log.warn("User {} attempted to exceed dialogue limit of {}", 
                currentUser.getId(), userLimitProperties.getMaxDialoguesPerUser());
            throw new DialogueLimitExceededException(userLimitProperties.getMaxDialoguesPerUser());
        }
        
        // Create new dialogue entity
        Dialogue dialogue = Dialogue.builder()
            .user(currentUser)
            .name(command.name() != null ? command.name() : "Whispered Dialogue")
            .sceneDescription(command.sceneDescription())
            .isGlobal(command.isGlobal() != null ? command.isGlobal() : false)
            .status(DialogueStatus.IN_PROGRESS)
            .build();
        
        // Save dialogue
        dialogue = dialogueRepository.save(dialogue);
        log.info("Created new dialogue entity: id={}, name={}", dialogue.getId(), dialogue.getName());
        
        // Create and save character configurations
        List<DialogueCharacterConfig> characterConfigs = createCharacterConfigs(command.characterConfigs(), dialogue);
        
        // Map to DTO and return
        return dialogueMapper.toDTO(dialogue, characterConfigs);
    }
    
    /**
     * Creates character configurations for a dialogue
     * 
     * @param characterConfigDtos the DTOs containing character and LLM references
     * @param dialogue the dialogue entity to associate with configurations
     * @return the list of created character configuration entities
     */
    private List<DialogueCharacterConfig> createCharacterConfigs(
            List<CharacterConfigDTO> characterConfigDtos, 
            Dialogue dialogue) {
        
        List<DialogueCharacterConfig> configs = new ArrayList<>();
        
        for (CharacterConfigDTO configDto : characterConfigDtos) {                        
            // Create DialogueCharacterConfig
            DialogueCharacterConfigId configId = new DialogueCharacterConfigId(
                dialogue.getId(),
                configDto.characterId()
            );
            
            DialogueCharacterConfig characterConfig = DialogueCharacterConfig.builder()
                .id(configId)
                .dialogue(dialogue)
                .character(characterQueryService.getCharacterEntity(configDto.characterId()))
                .llm(llmQueryService.findEntityById(configDto.llmId()))
                .build();
            
            // Save to repository
            configs.add(dialogueCharacterConfigRepository.save(characterConfig));
            log.trace("Created character config for character {} and LLM {} in dialogue {}", 
                    configDto.characterId(), configDto.llmId(), dialogue.getId());
        }
        
        log.debug("Created {} character configs for dialogue {}", configs.size(), dialogue.getId());
        return configs;
    }
} 