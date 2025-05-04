package com.github.vvojtas.dailogi_server.character.application;

import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.AvatarRepository;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.model.character.request.AvatarRequest;
import com.github.vvojtas.dailogi_server.service.util.AvatarUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterAvatarService {
    
    private final AvatarRepository avatarRepository;
    private final CharacterRepository characterRepository;
    private final CharacterValidator characterValidator;

    /**
     * Creates a new avatar and attaches it to a character
     * 
     * @param characterId The ID of the character
     * @param avatarRequest The avatar data
     * @return Optional containing the avatar if successful, empty otherwise
     */
    @Transactional
    public Optional<Avatar> createAndAttachAvatar(Long characterId, AvatarRequest avatarRequest) {
        if (avatarRequest == null) {
            return Optional.empty();
        }
        
        try {
            log.debug("Processing avatar data for character with id={}", characterId);
            
            // Get character
            Character character = characterValidator.getCharacterById(characterId);
            
            // Validate and decode avatar data
            byte[] avatarData = AvatarUtil.validateAndDecodeBase64Avatar(
                avatarRequest.data(), 
                avatarRequest.contentType()
            );
            
            // Create and save avatar entity
            Avatar avatar = Avatar.builder()
                .data(avatarData)
                .formatType(avatarRequest.contentType())
                .build();
            
            avatar = avatarRepository.save(avatar);
            character.setAvatar(avatar);
            characterRepository.save(character);
            
            log.debug("Avatar created and associated with character id={}", characterId);
            return Optional.of(avatar);
            
        } catch (ResourceNotFoundException e) {
            // Re-throw resource not found exceptions
            throw e;
        } catch (Exception e) {
            log.error("Failed to process avatar data for character id={}: {}", 
                characterId, e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed to process avatar data: " + e.getMessage()
            );
        }
    }
    
    /**
     * Updates an existing avatar or creates and attaches a new one
     * 
     * @param characterId The ID of the character
     * @param avatarRequest The avatar data
     * @return Optional containing the avatar if successful, empty otherwise
     */
    @Transactional
    public Optional<Avatar> updateOrAttachAvatar(Long characterId, AvatarRequest avatarRequest) {
        if (avatarRequest == null) {
            return Optional.empty();
        }
        
        try {
            log.debug("Processing avatar update for character with id={}", characterId);
            
            // Get character
            Character character = characterValidator.getCharacterById(characterId);
            
            // Validate and decode avatar data
            byte[] avatarData = AvatarUtil.validateAndDecodeBase64Avatar(
                avatarRequest.data(), 
                avatarRequest.contentType()
            );
            
            Avatar avatar;
            if (character.getAvatar() != null) {
                // Update existing avatar
                avatar = character.getAvatar();
                avatar.setData(avatarData);
                avatar.setFormatType(avatarRequest.contentType());
            } else {
                // Create new avatar
                avatar = Avatar.builder()
                    .data(avatarData)
                    .formatType(avatarRequest.contentType())
                    .build();
            }
            
            avatar = avatarRepository.save(avatar);
            character.setAvatar(avatar);
            characterRepository.save(character);
            
            log.debug("Avatar updated for character id={}", characterId);
            return Optional.of(avatar);
            
        } catch (ResourceNotFoundException e) {
            // Re-throw resource not found exceptions
            throw e;
        } catch (Exception e) {
            log.error("Failed to process avatar update for character id={}: {}", 
                characterId, e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed to process avatar update: " + e.getMessage()
            );
        }
    }
    
    /**
     * Removes an avatar from a character
     * 
     * @param characterId The ID of the character
     */
    @Transactional
    public void removeAvatar(Long characterId) {
        try {
            log.debug("Removing avatar from character with id={}", characterId);
            
            // Get character
            Character character = characterValidator.getCharacterById(characterId);
            
            if (character.getAvatar() != null) {
                Avatar avatar = character.getAvatar();
                character.setAvatar(null);
                characterRepository.save(character);
                avatarRepository.delete(avatar);
                log.debug("Avatar removed from character id={}", characterId);
            } else {
                log.debug("No avatar to remove for character id={}", characterId);
            }
            
        } catch (ResourceNotFoundException e) {
            // Re-throw resource not found exceptions
            throw e;
        } catch (Exception e) {
            log.error("Failed to remove avatar for character id={}: {}", 
                characterId, e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to remove avatar: " + e.getMessage()
            );
        }
    }
} 