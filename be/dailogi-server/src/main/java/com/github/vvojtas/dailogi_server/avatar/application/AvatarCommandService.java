package com.github.vvojtas.dailogi_server.avatar.application;

import com.github.vvojtas.dailogi_server.avatar.api.DeleteAvatarCommand;
import com.github.vvojtas.dailogi_server.avatar.api.UploadAvatarCommand;
import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AvatarRepository;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.model.character.request.AvatarRequest;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.service.util.AvatarUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarCommandService {
    private static final String AVATAR_RESOURCE_NAME = Avatar.class.getSimpleName().toLowerCase();

    private final AvatarRepository avatarRepository;
    private final CharacterRepository characterRepository;
    private final CurrentUserService currentUserService;
    private final AvatarValidator validator;

    /**
     * Creates a new avatar and attaches it to a character using base64 encoded data.
     * Used primarily during character creation process.
     * 
     * @param characterId The ID of the character
     * @param avatarRequest The avatar data in base64 format
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
            Character character = validator.validateCharacterExists(characterId);
            
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
     * Updates an existing avatar or creates and attaches a new one using base64 encoded data.
     * Used primarily during character update process.
     * 
     * @param characterId The ID of the character
     * @param avatarRequest The avatar data in base64 format
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
            Character character = validator.validateCharacterExists(characterId);
            
            // Validate and decode avatar data
            byte[] avatarData = AvatarUtil.validateAndDecodeBase64Avatar(
                avatarRequest.data(), 
                avatarRequest.contentType()
            );
            
            Avatar avatar;
            if (character.getAvatarId() != null) {
                // Update existing avatar
                avatar = avatarRepository.findById(character.getAvatarId())
                    .orElseThrow(() -> {
                        log.error("Inconsistency: Character {} has avatarId {} but Avatar entity not found.", 
                            characterId, character.getAvatarId());
                        return new IllegalStateException("Avatar data inconsistency for character " + characterId);
                    });
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
     * Removes an avatar from a character.
     * 
     * @param characterId The ID of the character
     */
    @Transactional
    public void removeAvatar(Long characterId) {
        try {
            log.debug("Removing avatar from character with id={}", characterId);
            
            // Get character
            Character character = validator.validateCharacterExists(characterId);
            
            if (character.getAvatarId() != null) {
                Avatar avatar = avatarRepository.findById(character.getAvatarId())
                    .orElseThrow(() -> {
                        log.error("Inconsistency: Character {} has avatarId {} but Avatar entity not found.", 
                            characterId, character.getAvatarId());
                        return new IllegalStateException("Avatar data inconsistency for character " + characterId);
                    });
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

    /**
     * Uploads or updates the avatar for a specific character using file upload.
     * Used by the avatar REST endpoint.
     *
     * @param characterId The ID of the character.
     * @param command The command object containing the avatar file.
     * @throws ResourceNotFoundException If the character is not found.
     * @throws IOException If there is an error reading the file.
     */
    @Transactional
    public void uploadOrUpdateAvatar(Long characterId, UploadAvatarCommand command) throws IOException {
        log.debug("Attempting to upload/update avatar for character id={}", characterId);

        // Validate the command
        validator.validateAvatarUpload(command);
        
        MultipartFile file = command.file();
        String formatType = AvatarUtil.validateAvatarFile(file);
        byte[] avatarData = file.getBytes();

        // Validate character exists and user has permission to modify
        Character character = validator.validateCharacterExists(characterId);
        AppUser currentUser = currentUserService.getCurrentAppUser();
        validator.validateCharacterOwnership(character, currentUser);

        Avatar avatar;
        if (character.getAvatarId() != null) {
            // Update existing avatar
            avatar = avatarRepository.findById(character.getAvatarId())
                .orElseThrow(() -> {
                    log.error("Inconsistency: Character {} has avatarId {} but Avatar entity not found.", 
                        characterId, character.getAvatarId());
                    // If inconsistent, treat as creating a new one
                    return new IllegalStateException("Avatar data inconsistency for character " + characterId);
                });
            avatar.setData(avatarData);
            avatar.setFormatType(formatType);
            log.debug("Updating existing avatar with id={} for character id={}", 
                avatar.getId(), characterId);
        } else {
            // Create new avatar
            avatar = Avatar.builder()
                .data(avatarData)
                .formatType(formatType)
                .build();
            log.debug("Creating new avatar for character id={}", characterId);
        }

        Avatar savedAvatar = avatarRepository.save(avatar);

        // Link avatar to character and save character IF the link has changed
        if (!savedAvatar.equals(character.getAvatar())) {
            character.setAvatar(savedAvatar);
            characterRepository.save(character);
            log.info("Successfully linked avatar id={} to character id={}", 
                savedAvatar.getId(), characterId);
        } else {
            log.info("Successfully updated avatar data for id={} linked to character id={}", 
                savedAvatar.getId(), characterId);
        }
    }

    /**
     * Deletes the avatar for a character via the REST endpoint.
     * 
     * @param command The command containing character ID and authentication
     */
    @Transactional
    public void deleteAvatar(DeleteAvatarCommand command) {
        log.debug("Attempting to delete avatar for character id={}", command.characterId());
        
        // Validate character exists and user has permission to modify
        Character character = validator.validateCharacterExists(command.characterId());
        AppUser currentUser = currentUserService.getCurrentAppUser(command.authentication());
        validator.validateCharacterOwnership(character, currentUser);
        
        // Validate character has an avatar
        if (character.getAvatarId() == null) {
            log.warn("Character id={} has no avatar to delete", command.characterId());
            throw new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                "Character with id " + command.characterId() + " has no avatar");
        }
        
        Long avatarId = character.getAvatarId();
        var avatar = avatarRepository.findById(avatarId)
            .orElseThrow(() -> {
                log.error("Data inconsistency: Character {} has avatarId {} but Avatar entity not found", 
                    command.characterId(), avatarId);
                return new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                    "Avatar not found with id: " + avatarId);
            });
        
        avatarRepository.delete(avatar);
        character.setAvatar(null);
        characterRepository.save(character);
        
        log.info("Successfully deleted avatar id={} for character id={}", 
            avatarId, command.characterId());
    }
} 