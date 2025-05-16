package com.github.vvojtas.dailogi_server.dialogue.application;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogueAuthorizationService {
    /**
     * Checks if a user can access a dialogue (is owned by user or is globally accessible)
     */
    public boolean canAccess(Dialogue dialogue, AppUser user) {
        // First check if it's globally accessible - this applies to all users, authenticated or not
        if (isGloballyAccessible(dialogue)) {
            return true;
        }
        
        // For non-global dialogues, check if the user owns it
        return isOwner(dialogue, user);
    }

    /**
     * Checks if a user can modify a dialogue (is owned by user)
     */
    public boolean canModify(Dialogue dialogue, AppUser user) {
        return isOwner(dialogue, user);
    }

    /**
     * Checks if a user can delete a dialogue (is owned by user)
     */
    public boolean canDelete(Dialogue dialogue, AppUser user) {
        return isOwner(dialogue, user);
    }

    /**
     * Checks if a dialogue is owned by a user
     */
    public boolean isOwner(Dialogue dialogue, AppUser user) {
        // If there's no user (unauthenticated), they cannot own the dialogue
        if (user == null || dialogue.getUser() == null) {
            return false;
        }
        return user.getId().equals(dialogue.getUser().getId());
    }

    /**
     * Checks if a dialogue is globally accessible
     */
    public boolean isGloballyAccessible(Dialogue dialogue) {
        return Boolean.TRUE.equals(dialogue.getIsGlobal());
    }
} 