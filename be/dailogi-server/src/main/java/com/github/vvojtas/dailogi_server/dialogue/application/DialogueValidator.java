package com.github.vvojtas.dailogi_server.dialogue.application;

import com.github.vvojtas.dailogi_server.character.application.CharacterAuthorizationService;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.db.repository.DialogueRepository;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
import com.github.vvojtas.dailogi_server.dialogue.api.CreateDialogueCommand;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator for dialogue commands
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DialogueValidator {
    private static final String DIALOGUE_RESOURCE_NAME = Dialogue.class.getSimpleName().toLowerCase();
    private static final String CHARACTER_RESOURCE_NAME = Character.class.getSimpleName().toLowerCase();
    private static final String LLM_RESOURCE_NAME = LLM.class.getSimpleName().toLowerCase();
    
    private final DialogueRepository dialogueRepository;
    private final CharacterRepository characterRepository;
    private final LLMRepository llmRepository;
    private final CharacterAuthorizationService characterAuthorizationService;
    
    /**
     * Validates a dialogue creation command
     * 
     * @param command The command to validate
     * @param currentUser The current user
     * @throws ResourceNotFoundException if a character or LLM doesn't exist
     * @throws AccessDeniedException if the user doesn't have access to a character
     */
    public void validateForCreation(CreateDialogueCommand command, AppUser currentUser) {
        validateCharacterConfigs(command.characterConfigs(), currentUser);
    }
    
    /**
     * Validates that all characters exist and are accessible by the user
     * and that all LLMs exist
     * 
     * @param configs The character configs to validate
     * @param currentUser The current user
     */
    private void validateCharacterConfigs(List<CharacterConfigDTO> configs, AppUser currentUser) {
        for (CharacterConfigDTO config : configs) {
            // Validate character exists and is accessible
            Character character = characterRepository.findById(config.characterId())
                .orElseThrow(() -> {
                    log.warn("Attempt to use non-existent character with id={}", config.characterId());
                    return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME, 
                        "Character not found with id: " + config.characterId());
                });
            
            // Validate access to character
            if (!characterAuthorizationService.canAccess(character, currentUser)) {
                log.warn("User {} attempted to access character {} which they don't have access to", 
                    currentUser.getId(), character.getId());
                throw new AccessDeniedException(
                    "User does not have permission to access this character");
            }
            
            // Validate LLM exists
            if (!llmRepository.existsById(config.llmId())) {
                log.warn("Attempted to use non-existent LLM id={}", config.llmId());
                throw new ResourceNotFoundException(LLM_RESOURCE_NAME, 
                    "LLM not found with id: " + config.llmId());
            }
        }
    }
    
    /**
     * Gets a dialogue by ID, validating its existence
     * 
     * @param id The dialogue ID
     * @return The dialogue entity
     * @throws ResourceNotFoundException if dialogue doesn't exist
     */
    public Dialogue getDialogueById(Long id) {
        return dialogueRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Dialogue not found with id={}", id);
                return new ResourceNotFoundException(DIALOGUE_RESOURCE_NAME,
                    "Dialogue not found with id: " + id);
            });
    }
} 