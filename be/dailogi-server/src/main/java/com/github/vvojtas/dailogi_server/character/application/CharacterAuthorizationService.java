package com.github.vvojtas.dailogi_server.character.application;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterAuthorizationService {
    /**
     * Checks if a user can access a character (is owned by user or is globally accessible)
     */
    public boolean canAccess(Character character, AppUser user) {
        return isOwner(character, user) || isGloballyAccessible(character);
    }

    /**
     * Checks if a user can modify a character (is owned by user)
     */
    public boolean canModify(Character character, AppUser user) {
        return isOwner(character, user);
    }

    /**
     * Checks if a user can delete a character (is owned by user)
     */
    public boolean canDelete(Character character, AppUser user) {
        return isOwner(character, user);
    }

    /**
     * Checks if a character is owned by a user
     */
    public boolean isOwner(Character character, AppUser user) {
        // If there's no user (unauthenticated), they cannot own the character
        if (user == null) {
            return false;
        }
        return user.getId().equals(character.getUser().getId());
    }

    /**
     * Checks if a character is globally accessible
     */
    public boolean isGloballyAccessible(Character character) {
        return Boolean.TRUE.equals(character.getIsGlobal());
    }
} 