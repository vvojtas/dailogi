package com.github.vvojtas.dailogi_server.avatar.application;

import com.github.vvojtas.dailogi_server.avatar.api.UploadAvatarCommand;
import com.github.vvojtas.dailogi_server.character.application.CharacterAuthorizationService;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarValidator {
    private static final String CHARACTER_RESOURCE_NAME = Character.class.getSimpleName().toLowerCase();
    
    private final CharacterRepository characterRepository;
    private final CharacterAuthorizationService authorizationService;
    
    public Character validateCharacterExists(Long characterId) {
        return characterRepository.findById(characterId)
            .orElseThrow(() -> {
                log.warn("Character not found with id={}", characterId);
                return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME, 
                    "Character not found with id: " + characterId);
            });
    }
    
    public void validateCharacterAccess(Character character, AppUser user) {
        if (!authorizationService.canAccess(character, user)) {
            String currentUserId = (user != null) ? user.getId().toString() : "unauthenticated";
            log.warn("Authorization failed: User {} attempted to access avatar for character {} (Owner: {}, Global: {})", 
                currentUserId, character.getId(), 
                character.getUser() != null ? character.getUser().getId() : "null", 
                character.getIsGlobal());
            throw new AccessDeniedException("User does not have access to this character's avatar");
        }
    }
    
    public void validateCharacterOwnership(Character character, AppUser user) {
        if (!authorizationService.canModify(character, user)) {
            log.warn("User {} attempted to modify avatar for character {} owned by user {}",
                user.getId(), character.getId(), 
                character.getUser() != null ? character.getUser().getId() : "null");
            throw new AccessDeniedException("User does not have permission to modify avatar for this character");
        }
    }
    
    public void validateAvatarUpload(UploadAvatarCommand command) {
        if (command.file() == null || command.file().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is missing or empty.");
        }
    }
} 