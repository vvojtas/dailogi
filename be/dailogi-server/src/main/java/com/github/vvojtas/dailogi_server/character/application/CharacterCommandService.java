package com.github.vvojtas.dailogi_server.character.application;

import com.github.vvojtas.dailogi_server.character.api.CreateCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.DeleteCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.UpdateCharacterCommand;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterMapper;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterCommandService {
    private final CharacterRepository characterRepository;
    private final LLMRepository llmRepository;
    private final CharacterValidator validator;
    private final CharacterAvatarService avatarService;
    private final CharacterMapper characterMapper;
    private final CurrentUserService currentUserService;

    /**
     * Creates a new character for the current user.
     * 
     * @param command the command containing character creation data
     * @return the created character as DTO
     */
    @Transactional
    public CharacterDTO createCharacter(CreateCharacterCommand command) {
        log.debug("Creating character with name={}", command.name());
        
        AppUser currentUser = currentUserService.getCurrentAppUser();
        log.debug("Creating character for user {}", currentUser.getId());

        // Validate command
        validator.validateForCreation(command, currentUser);
        
        // Create character entity
        Character character = Character.builder()
            .user(currentUser)
            .name(command.name())
            .shortDescription(command.shortDescription())
            .description(command.description())
            .isGlobal(false)
            .build();
        
        // Set default LLM if specified
        if (command.defaultLlmId() != null) {
            LLM defaultLlm = llmRepository.findById(command.defaultLlmId()).orElseThrow();
            character.setDefaultLlm(defaultLlm);
        }
        
        // Save character
        character = characterRepository.save(character);
        
        // Handle avatar if provided
        if (command.avatar() != null) {
            Optional<Avatar> avatar = avatarService.updateOrAttachAvatar(character.getId(), command.avatar());
            character.setAvatar(avatar.orElse(null));
        }
        
        log.info("Created new character: id={}, name={}, with avatar={}", 
            character.getId(), character.getName(), character.getAvatar() != null);
        
        return characterMapper.toDTO(character);
    }

    /**
     * Updates an existing character with new data.
     * 
     * @param command the command containing character update data
     * @return the updated character as DTO
     */
    @Transactional
    public CharacterDTO updateCharacter(UpdateCharacterCommand command) {
        log.debug("Updating character with id={}, name={}", command.id(), command.name());
        
        AppUser currentUser = currentUserService.getCurrentAppUser();
        
        // Validate command
        validator.validateForUpdate(command, currentUser);
        
        // Get character
        Character character = validator.getCharacterById(command.id());
        
        // Update character fields
        character.setName(command.name())
                .setShortDescription(command.shortDescription())
                .setDescription(command.description());
        
        // Update default LLM
        if (command.defaultLlmId() != null) {
            LLM defaultLlm = llmRepository.findById(command.defaultLlmId()).orElseThrow();
            character.setDefaultLlm(defaultLlm);
        } else {
            character.setDefaultLlm(null);
        }
        
        // Save character
        character = characterRepository.save(character);
        
        // Handle avatar if provided
        if (command.avatar() != null) {
            Optional<Avatar> avatar = avatarService.updateOrAttachAvatar(character.getId(), command.avatar());
            character.setAvatar(avatar.orElse(null));
        }
        
        log.info("Updated character: id={}, name={}", character.getId(), character.getName());
        
        return characterMapper.toDTO(character);
    }

    /**
     * Deletes a character owned by the current user.
     * 
     * @param command the command containing the character ID to delete
     */
    @Transactional
    public void deleteCharacter(DeleteCharacterCommand command) {
        log.debug("Starting deletion process for character with id={}", command.id());
        
        AppUser currentUser = currentUserService.getCurrentAppUser();
        
        // Validate command
        validator.validateForDeletion(command, currentUser);
        
        // Get character
        Character character = validator.getCharacterById(command.id());
        
        // Perform deletion
        try {
            log.debug("Deleting character: id={}, name={}", command.id(), character.getName());
            characterRepository.delete(character);
            log.info("Successfully deleted character: id={}, name={}", command.id(), character.getName());
        } catch (Exception e) {
            log.error("Failed to delete character {}: {}", command.id(), e.getMessage(), e);
            throw e;
        }
    }
} 