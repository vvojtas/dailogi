package com.github.vvojtas.dailogi_server.dialogue.application;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.DialogueMessage;
import com.github.vvojtas.dailogi_server.db.repository.DialogueMessageRepository;
import com.github.vvojtas.dailogi_server.dialogue.api.DialogueMessageSaveCommand;
import com.github.vvojtas.dailogi_server.dialogue.application.DialogueValidator.DialogueValidationResult;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DialogueMessageCommandServiceTest {

    private static final Long DIALOGUE_ID = 1L;
    private static final Long CHARACTER_ID = 2L;
    private static final int MESSAGE_SEQUENCE_NUMBER = 3;
    private static final String MESSAGE_CONTENT = "Test message content";
    private static final Long SAVED_MESSAGE_ID = 5L;
    
    @Mock
    private DialogueMessageRepository dialogueMessageRepository;
    
    @Mock
    private CurrentUserService currentUserService;
    
    @Mock
    private DialogueValidator dialogueValidator;
    
    @Mock
    private AppUser currentUser;
    
    @Mock
    private Dialogue dialogue;
    
    @Mock
    private Character character;
    
    @Captor
    private ArgumentCaptor<DialogueMessage> dialogueMessageCaptor;
    
    private DialogueMessageCommandService service;
    
    @BeforeEach
    void setUp() {
        service = new DialogueMessageCommandService(
                dialogueMessageRepository,
                currentUserService,
                dialogueValidator
        );
    }
    
    @Test
    void saveMessage_shouldSaveValidMessage() {
        // Given
        DialogueMessageSaveCommand command = new DialogueMessageSaveCommand(
                CHARACTER_ID,
                MESSAGE_CONTENT,
                MESSAGE_SEQUENCE_NUMBER
        );
        
        DialogueValidationResult validationResult = new DialogueValidationResult(dialogue, character);
        
        DialogueMessage savedMessage = mock(DialogueMessage.class);
        when(savedMessage.getId()).thenReturn(SAVED_MESSAGE_ID);
        
        // Setup mocks
        when(currentUserService.getCurrentAppUser()).thenReturn(currentUser);
        when(dialogueValidator.validateForMessageSave(DIALOGUE_ID, command, currentUser))
                .thenReturn(validationResult);
        when(dialogueMessageRepository.save(any(DialogueMessage.class)))
                .thenReturn(savedMessage);
        
        // When
        Long result = service.saveMessage(DIALOGUE_ID, command);
        
        // Then
        verify(dialogueValidator).validateForMessageSave(DIALOGUE_ID, command, currentUser);
        verify(dialogueMessageRepository).save(dialogueMessageCaptor.capture());
        
        DialogueMessage capturedMessage = dialogueMessageCaptor.getValue();
        assertEquals(dialogue, capturedMessage.getDialogue());
        assertEquals(character, capturedMessage.getCharacter());
        assertEquals(CHARACTER_ID, capturedMessage.getCharacterId());
        assertEquals(MESSAGE_SEQUENCE_NUMBER, capturedMessage.getTurnNumber());
        assertEquals(MESSAGE_CONTENT, capturedMessage.getContent());
        
        assertEquals(SAVED_MESSAGE_ID, result);
    }
    
    @Test
    void saveMessage_shouldPropagateValidationException() {
        // Given
        DialogueMessageSaveCommand command = new DialogueMessageSaveCommand(
                CHARACTER_ID,
                MESSAGE_CONTENT,
                MESSAGE_SEQUENCE_NUMBER
        );
        
        // Setup mocks
        when(currentUserService.getCurrentAppUser()).thenReturn(currentUser);
        when(dialogueValidator.validateForMessageSave(DIALOGUE_ID, command, currentUser))
                .thenThrow(new AccessDeniedException("Access denied"));
        
        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            service.saveMessage(DIALOGUE_ID, command);
        });
        
        verify(dialogueMessageRepository, never()).save(any(DialogueMessage.class));
    }
} 