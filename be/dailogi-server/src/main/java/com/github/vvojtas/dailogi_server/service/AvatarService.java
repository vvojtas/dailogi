package com.github.vvojtas.dailogi_server.service;

import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AvatarRepository;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.model.avatar.AvatarData;
import com.github.vvojtas.dailogi_server.model.avatar.request.UploadAvatarCommand;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private final CharacterRepository characterRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public AvatarData getAvatarData(Long characterId, Authentication authentication) {
        log.debug("Attempting to retrieve avatar data for character id={}", characterId);
        
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> {
                 log.warn("Avatar request failed: Character not found with id={}", characterId);
                 return new ResourceNotFoundException("character", "Character not found with id: " + characterId);
            });

        // Check if the character is global or owned by the current user
        AppUser currentUser = currentUserService.getCurrentAppUser(authentication);
        if (!CharacterService.isOwnedOrGlobal(character, currentUser)) {
             String currentUserId = (currentUser != null) ? currentUser.getId().toString() : "unauthenticated";
             log.warn("Authorization failed: User {} attempted to access avatar for character {} (Owner: {}, Global: {})", 
                 currentUserId, characterId, character.getUser() != null ? character.getUser().getId() : "null", character.getIsGlobal());
            throw new AccessDeniedException("User does not have access to this character's avatar");
        }

        // Check if character has an avatar using the avatarId field
        var avatarId = character.getAvatarId();
        if (avatarId== null) {
            log.debug("Character id={} found, but has no avatar (avatarId is null).", characterId);
            throw new ResourceNotFoundException("avatar", "Character wit id: " + characterId + "has no avatar");
        }

        // Fetch the avatar data using avatarId
        log.debug("Fetching avatar id={} for character id={}", avatarId, characterId);
        AvatarData avatarData = avatarRepository.findById(avatarId)
            .map(avatar -> new AvatarData(avatar.getData(), avatar.getFormatType()))
            .orElseThrow(() -> {
                 // This case indicates data inconsistency
                 log.error("Data inconsistency: Character {} has avatarId {} but Avatar entity not found.", characterId, avatarId);
                 return new ResourceNotFoundException("avatar", "Avatar not found with id: " + avatarId);
             });
             
        log.info("Successfully retrieved avatar id={} for character id={}",  avatarId, characterId);
        return avatarData;
    }
    
    /**
     * Uploads or updates the avatar for a specific character.
     * Validates the file and ensures the current user owns the character.
     *
     * @param characterId The ID of the character.
     * @param command The command object containing the avatar file.
     * @throws ResourceNotFoundException If the character is not found.
     * @throws AccessDeniedException If the user does not own the character.
     * @throws ResponseStatusException If the uploaded file is invalid (type, size, dimensions).
     * @throws IOException If there is an error reading the file.
     */
    @Transactional
    public void uploadOrUpdateAvatar(Long characterId, UploadAvatarCommand command) throws IOException {
        log.debug("Attempting to upload/update avatar for character id={}", characterId);

        MultipartFile file = command.file();
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is missing or empty.");
        }

        // Validate file type, size, dimensions
        String formatType = validateAvatarFile(file);
        byte[] avatarData = file.getBytes();

        // Validate character exists and is owned by current user
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> {
                log.warn("Attempt to set avatar for non-existent character with id={}", characterId);
                return new ResourceNotFoundException("character", "Character not found with id: " + characterId);
            });

        // Use CurrentUserService to get the authenticated user for ownership check
        AppUser currentUser = currentUserService.getCurrentAppUser(); 
        if (currentUser == null) {
             // Should not happen if endpoint is secured with PreAuthorize("isAuthenticated()")
             log.error("Avatar upload attempt by unauthenticated user for character {}", characterId);
             throw new AccessDeniedException("User must be authenticated to upload an avatar.");
        }

        // Validate ownership using the static method from CharacterService
        if (!CharacterService.isOwned(character, currentUser)) {
            log.warn("User {} attempted to set avatar for character {} owned by user {}",
                currentUser.getId(), characterId, character.getUser() != null ? character.getUser().getId() : "null");
            throw new AccessDeniedException("User does not have permission to set avatar for this character");
        }

        Avatar avatar;
        if (character.getAvatarId() != null) {
            // Update existing avatar
            avatar = avatarRepository.findById(character.getAvatarId())
                .orElseThrow(() -> {
                    log.error("Inconsistency: Character {} has avatarId {} but Avatar entity not found.", characterId, character.getAvatarId());
                    // If inconsistent, treat as creating a new one
                    return new IllegalStateException("Avatar data inconsistency for character " + characterId);
                });
            avatar.setData(avatarData);
            avatar.setFormatType(formatType); // Use validated format type
            log.debug("Updating existing avatar with id={} for character id={}", avatar.getId(), characterId);
        } else {
            // Create new avatar
            avatar = Avatar.builder()
                .data(avatarData)
                .formatType(formatType) // Use validated format type
                .build();
            log.debug("Creating new avatar for character id={}", characterId);
        }

        Avatar savedAvatar = avatarRepository.save(avatar);

        // Link avatar to character and save character IF the link has changed
        if (!savedAvatar.equals(character.getAvatar())) {
            character.setAvatar(savedAvatar);
            characterRepository.save(character); // Save character to update the avatar_id foreign key
            log.info("Successfully linked avatar id={} to character id={}", savedAvatar.getId(), characterId);
        } else {
            log.info("Successfully updated avatar data for id={} linked to character id={}", savedAvatar.getId(), characterId);
        }
    }

    /**
     * Validates the uploaded avatar file based on type, size, and dimensions.
     * Moved from CharacterService.
     *
     * @param file The uploaded MultipartFile.
     * @return The validated content type (e.g., "image/png", "image/jpeg").
     * @throws ResponseStatusException If validation fails.
     * @throws IOException If the file cannot be read.
     */
    private String validateAvatarFile(MultipartFile file) throws IOException {
        // Validate file type (Allow PNG and JPEG)
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            log.warn("Invalid file type '{}' for avatar upload. Allowed: image/png, image/jpeg", contentType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only PNG and JPEG files are allowed.");
        }

        // Validate file size (1MB = 1048576 bytes)
        if (file.getSize() > 1048576) {
            log.warn("File size {} bytes exceeds maximum allowed size of 1MB", file.getSize());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must not exceed 1MB.");
        }

        // Read and validate image dimensions (Max 256x256)
        BufferedImage img = ImageIO.read(file.getInputStream());
        if (img == null) {
            log.warn("Failed to read image file, it might be corrupted or not a valid image.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file. Could not read image data.");
        }

        if (img.getWidth() > 256 || img.getHeight() > 256) {
            log.warn("Invalid image dimensions: {}x{}. Maximum allowed is 256x256.", img.getWidth(), img.getHeight());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image dimensions must not exceed 256x256 pixels.");
        }

        log.debug("Avatar file validation successful: Type={}, Size={} bytes, Dimensions={}x{}",
            contentType, file.getSize(), img.getWidth(), img.getHeight());

        return contentType; // Return the validated content type
    }

    @Transactional
    public void deleteAvatar(Long characterId, Authentication authentication) {
        log.debug("Attempting to delete avatar for character id={}", characterId);
        
        var character = characterRepository.findById(characterId)
            .orElseThrow(() -> {
                log.warn("Character not found with id={}", characterId);
                return new ResourceNotFoundException("character", "Character not found with id: " + characterId);
            });
        
        var currentUser = currentUserService.getCurrentAppUser(authentication);
        if (!CharacterService.isOwned(character, currentUser)) {
            log.warn("User {} attempted to delete avatar for character {} without proper ownership", currentUser.getId(), characterId);
            throw new AccessDeniedException("User does not have permission to delete avatar for this character");
        }
        
        if (character.getAvatarId() == null) {
            log.warn("Character id={} has no avatar to delete", characterId);
            throw new ResourceNotFoundException("avatar", "Character with id " + characterId + " has no avatar");
        }
        
        Long avatarId = character.getAvatarId();
        var avatar = avatarRepository.findById(avatarId)
            .orElseThrow(() -> {
                log.error("Data inconsistency: Character {} has avatarId {} but Avatar entity not found", characterId, avatarId);
                return new ResourceNotFoundException("avatar", "Avatar not found with id: " + avatarId);
            });
        
        avatarRepository.delete(avatar);
        character.setAvatar(null);
        characterRepository.save(character);
        
        log.info("Successfully deleted avatar id={} for character id={}", avatarId, characterId);
    }
} 