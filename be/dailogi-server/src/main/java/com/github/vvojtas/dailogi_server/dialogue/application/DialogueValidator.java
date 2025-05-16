package com.github.vvojtas.dailogi_server.dialogue.application;

import com.github.vvojtas.dailogi_server.character.application.CharacterAuthorizationService;
import com.github.vvojtas.dailogi_server.character.application.CharacterQueryService;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.db.repository.DialogueRepository;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
import com.github.vvojtas.dailogi_server.dialogue.api.CreateDialogueCommand;
import com.github.vvojtas.dailogi_server.dialogue.api.DialogueMessageSaveCommand;
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
    private final CharacterQueryService characterQueryService;
    private final DialogueAuthorizationService authorizationService;
    
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
    
    /**
     * Validates a message save operation for a dialogue
     * 
     * @param dialogueId the ID of the dialogue
     * @param command the message save command
     * @param currentUser the current user
     * @return the validated dialogue and character entities
     * @throws ResourceNotFoundException if dialogue or character doesn't exist
     * @throws AccessDeniedException if user doesn't own the dialogue
     */
    public DialogueValidationResult validateForMessageSave(Long dialogueId, DialogueMessageSaveCommand command, AppUser currentUser) {
        // Validate dialogue exists
        Dialogue dialogue = dialogueRepository.findById(dialogueId)
            .orElseThrow(() -> {
                log.warn("Attempt to save message to non-existent dialogue with id={}", dialogueId);
                return new ResourceNotFoundException(DIALOGUE_RESOURCE_NAME, 
                    "Dialogue not found with id: " + dialogueId);
            });
        
        // Validate ownership
        if (!authorizationService.canModify(dialogue, currentUser)) {
            log.warn("User {} attempted to save message to dialogue {} owned by user {}", 
                currentUser.getId(), dialogueId, dialogue.getUser().getId());
            throw new AccessDeniedException(
                "Cannot save message to a dialogue you don't own");
        }
        
        // Validate character exists
        Character character = characterQueryService.getCharacterEntity(command.characterId());
        
        return new DialogueValidationResult(dialogue, character);
    }
    
    /**
     * Validates a status update operation for a dialogue
     * 
     * @param dialogueId the ID of the dialogue
     * @param currentUser the current user
     * @return the validated dialogue entity
     * @throws ResourceNotFoundException if dialogue doesn't exist
     * @throws AccessDeniedException if user doesn't own the dialogue
     */
    public Dialogue validateForStatusUpdate(Long dialogueId, AppUser currentUser) {
        // Validate dialogue exists
        Dialogue dialogue = dialogueRepository.findById(dialogueId)
            .orElseThrow(() -> {
                log.warn("Attempt to update status of non-existent dialogue with id={}", dialogueId);
                return new ResourceNotFoundException(DIALOGUE_RESOURCE_NAME, 
                    "Dialogue not found with id: " + dialogueId);
            });
        
        // Validate ownership
        if (!authorizationService.canModify(dialogue, currentUser)) {
            log.warn("User {} attempted to update status of dialogue {} owned by user {}", 
                currentUser.getId(), dialogueId, dialogue.getUser().getId());
            throw new AccessDeniedException(
                "Cannot update status of a dialogue you don't own");
        }
        
        return dialogue;
    }
    
    /**
     * Container for validated dialogue entities
     */
    public record DialogueValidationResult(Dialogue dialogue, Character character) {}
} 