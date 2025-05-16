package com.github.vvojtas.dailogi_server.dialogue.application;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.DialogueMessage;
import com.github.vvojtas.dailogi_server.db.repository.DialogueMessageRepository;
import com.github.vvojtas.dailogi_server.dialogue.api.DialogueMessageSaveCommand;
import com.github.vvojtas.dailogi_server.dialogue.application.DialogueValidator.DialogueValidationResult;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for saving dialogue messages
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DialogueMessageCommandService {

    private final DialogueMessageRepository dialogueMessageRepository;
    private final CurrentUserService currentUserService;
    private final DialogueValidator dialogueValidator;
    
    /**
     * Saves a message for a specific dialogue
     * 
     * @param dialogueId the ID of the dialogue
     * @param messageDto the message data to save
     * @return the ID of the saved message
     * @throws ResourceNotFoundException if the dialogue or character doesn't exist
     * @throws AccessDeniedException if the current user doesn't own the dialogue
     */
    @Transactional
    public Long saveMessage(Long dialogueId, DialogueMessageSaveCommand messageDto) {
        log.debug("Saving message for dialogue ID {} from character ID {}", dialogueId, messageDto.characterId());
        
        // Get the current user
        AppUser currentUser = currentUserService.getCurrentAppUser();
        
        // Validate and get dialogue and character entities
        DialogueValidationResult validationResult = dialogueValidator.validateForMessageSave(dialogueId, messageDto, currentUser);
        Dialogue dialogue = validationResult.dialogue();
        Character character = validationResult.character();
                
        // Create and save the message entity
        DialogueMessage message = DialogueMessage.builder()
                .dialogue(dialogue)
                .character(character)
                .characterId(messageDto.characterId()) // Make sure to set this explicitly
                .turnNumber(messageDto.messageSequenceNumber())
                .content(messageDto.content())
                .build();
        
        // Save to database
        DialogueMessage savedMessage = dialogueMessageRepository.save(message);
        log.info("Saved dialogue message ID {} for dialogue {} from character {}", 
                savedMessage.getId(), dialogueId, messageDto.characterId());
        
        return savedMessage.getId();
    }
} 