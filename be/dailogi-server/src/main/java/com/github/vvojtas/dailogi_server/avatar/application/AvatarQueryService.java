package com.github.vvojtas.dailogi_server.avatar.application;

import com.github.vvojtas.dailogi_server.avatar.api.GetAvatarQuery;
import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AvatarRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.model.avatar.AvatarData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarQueryService {
    private static final String AVATAR_RESOURCE_NAME = Avatar.class.getSimpleName().toLowerCase();

    private final AvatarRepository avatarRepository;
    private final CurrentUserService currentUserService;
    private final AvatarValidator validator;

    public AvatarData getAvatarData(GetAvatarQuery query) {
        log.debug("Attempting to retrieve avatar data for character id={}", query.characterId());
        
        // Validate the character and access authorization
        Character character = validator.validateCharacterExists(query.characterId());
        
        // Validate user has access to character
        AppUser currentUser = currentUserService.getCurrentAppUser(query.authentication());
        validator.validateCharacterAccess(character, currentUser);

        // Check if character has an avatar
        var avatarId = character.getAvatarId();
        if (avatarId == null) {
            log.debug("Character id={} found, but has no avatar (avatarId is null).", query.characterId());
            throw new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                "Character with id: " + query.characterId() + " has no avatar");
        }

        // Fetch the avatar data using avatarId
        log.debug("Fetching avatar id={} for character id={}", avatarId, query.characterId());
        AvatarData avatarData = avatarRepository.findById(avatarId)
            .map(avatar -> new AvatarData(avatar.getData(), avatar.getFormatType()))
            .orElseThrow(() -> {
                 // This case indicates data inconsistency
                 log.error("Data inconsistency: Character {} has avatarId {} but Avatar entity not found.", 
                     query.characterId(), avatarId);
                 return new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                     "Avatar not found with id: " + avatarId);
             });
             
        log.info("Successfully retrieved avatar id={} for character id={}", 
            avatarId, query.characterId());
        return avatarData;
    }
} 