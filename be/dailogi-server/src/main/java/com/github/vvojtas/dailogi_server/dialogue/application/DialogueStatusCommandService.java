package com.github.vvojtas.dailogi_server.dialogue.application;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.DialogueStatus;
import com.github.vvojtas.dailogi_server.db.repository.DialogueRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for updating dialogue status
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DialogueStatusCommandService {

    private final DialogueRepository dialogueRepository;
    private final CurrentUserService currentUserService;
    private final DialogueValidator dialogueValidator;
    
    /**
     * Updates the status of a dialogue
     * 
     * @param dialogueId the ID of the dialogue
     * @param status the new status
     * @return true if the update was successful
     * @throws ResourceNotFoundException if the dialogue doesn't exist
     * @throws AccessDeniedException if the current user doesn't own the dialogue
     */
    @Transactional
    public boolean updateStatus(Long dialogueId, DialogueStatus status) {
        log.debug("Updating status for dialogue ID {} to {}", dialogueId, status);
        
        // Get the current user
        AppUser currentUser = currentUserService.getCurrentAppUser();
        
        // Validate and get dialogue entity
        Dialogue dialogue = dialogueValidator.validateForStatusUpdate(dialogueId, currentUser);
        
        // Update the status
        dialogue.setStatus(status);
        dialogueRepository.save(dialogue);
        log.info("Updated status of dialogue {} to {}", dialogueId, status);
        
        return true;
    }
} 