package com.github.vvojtas.dailogi_server.character.application;

import com.github.vvojtas.dailogi_server.character.api.CreateCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.DeleteCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.UpdateCharacterCommand;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.exception.CharacterLimitExceededException;
import com.github.vvojtas.dailogi_server.exception.DuplicateResourceException;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.properties.UserLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CharacterValidator {
    private static final String CHARACTER_RESOURCE_NAME = Character.class.getSimpleName().toLowerCase();
    private static final String LLM_RESOURCE_NAME = LLM.class.getSimpleName().toLowerCase();

    private final CharacterRepository characterRepository;
    private final LLMRepository llmRepository;
    private final UserLimitProperties userLimitProperties;
    private final CharacterAuthorizationService authorizationService;
    /**
     * Validates a character creation command
     * 
     * @param command The command to validate
     * @throws CharacterLimitExceededException if user has exceeded character limit
     * @throws DuplicateResourceException if character with same name exists
     * @throws ResourceNotFoundException if specified LLM doesn't exist
     */
    public void validateForCreation(CreateCharacterCommand command, AppUser currentUser) {
        // Check if user has reached the character limit
        long userCharacterCount = characterRepository.countByUser(currentUser);
        if (userCharacterCount >= userLimitProperties.getMaxCharactersPerUser()) {
            log.warn("User {} attempted to exceed character limit of {}", 
                currentUser.getId(), userLimitProperties.getMaxCharactersPerUser());
            throw new CharacterLimitExceededException(userLimitProperties.getMaxCharactersPerUser());
        }
        
        // Verify character name uniqueness for this user
        if (characterRepository.existsByNameAndUser(command.name(), currentUser)) {
            log.warn("User {} attempted to create duplicate character with name '{}'", 
                currentUser.getId(), command.name());
            throw new DuplicateResourceException(CHARACTER_RESOURCE_NAME, 
                "Character with name '" + command.name() + "' already exists");
        }
        
        // Validate LLM if specified
        if (command.defaultLlmId() != null && !llmRepository.existsById(command.defaultLlmId())) {
            log.warn("Attempted to set non-existent LLM id={} as default", command.defaultLlmId());
            throw new ResourceNotFoundException(LLM_RESOURCE_NAME, 
                "LLM not found with id: " + command.defaultLlmId());
        }
    }
    
    /**
     * Validates a character update command
     * 
     * @param command The command to validate
     * @throws ResourceNotFoundException if character or LLM doesn't exist
     * @throws AccessDeniedException if user doesn't own the character
     * @throws DuplicateResourceException if character with same name exists
     */
    public void validateForUpdate(UpdateCharacterCommand command, AppUser currentUser) {
        // Validate character exists
        Character character = characterRepository.findById(command.id())
            .orElseThrow(() -> {
                log.warn("Attempt to update non-existent character with id={}", command.id());
                return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME, 
                    "Character not found with id: " + command.id());
            });
        
        // Validate ownership
        if (!authorizationService.canModify(character, currentUser)) {
            log.warn("User {} attempted to update character {} owned by user {}", 
                currentUser.getId(), command.id(), character.getUser().getId());
            throw new AccessDeniedException(
                "User does not have permission to update this character");
        }
        
        // Verify character name uniqueness (excluding the current character)
        if (!command.name().equals(character.getName()) && 
            characterRepository.existsByNameAndUser(command.name(), currentUser)) {
            log.warn("User {} attempted to rename character {} to existing name '{}'", 
                currentUser.getId(), command.id(), command.name());
            throw new DuplicateResourceException(CHARACTER_RESOURCE_NAME, 
                "Character with name '" + command.name() + "' already exists");
        }
        
        // Validate LLM if specified
        if (command.defaultLlmId() != null && !llmRepository.existsById(command.defaultLlmId())) {
            log.warn("Attempted to set non-existent LLM id={} as default", command.defaultLlmId());
            throw new ResourceNotFoundException(LLM_RESOURCE_NAME, 
                "LLM not found with id: " + command.defaultLlmId());
        }
    }
    
    /**
     * Validates a character deletion command
     * 
     * @param command The command to validate
     * @throws ResourceNotFoundException if character doesn't exist
     * @throws AccessDeniedException if user doesn't own the character
     * @throws ResponseStatusException with CONFLICT if character is used in dialogues
     */
    public void validateForDeletion(DeleteCharacterCommand command, AppUser currentUser) {
        // Validate character exists
        Character character = characterRepository.findById(command.id())
            .orElseThrow(() -> {
                log.warn("Attempt to delete non-existent character with id={}", command.id());
                return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME, 
                    "Character not found with id: " + command.id());
            });
        
        // Validate ownership
        if (!authorizationService.canDelete(character, currentUser)) {
            log.warn("User {} attempted to delete character {} owned by user {}", 
                currentUser.getId(), command.id(), character.getUser().getId());
            throw new AccessDeniedException(
                "User does not have permission to delete this character");
        }
        
        // Check for dialogue references
        if (characterRepository.existsInDialogues(command.id())) {
            log.warn("Cannot delete character {} as it is used in dialogues", command.id());
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Character cannot be deleted because it is used in one or more dialogues"
            );
        }
    }
    
    /**
     * Gets a character by ID, validating its existence
     * 
     * @param id The character ID
     * @return The character entity
     * @throws ResourceNotFoundException if character doesn't exist
     */
    public Character getCharacterById(Long id) {
        return characterRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Character not found with id={}", id);
                return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME,
                    "Character not found with id: " + id);
            });
    }
} 