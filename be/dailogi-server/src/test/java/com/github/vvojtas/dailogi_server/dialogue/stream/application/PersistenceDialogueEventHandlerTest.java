package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.db.entity.DialogueStatus;
import com.github.vvojtas.dailogi_server.dialogue.api.DialogueMessageSaveCommand;
import com.github.vvojtas.dailogi_server.dialogue.application.DialogueMessageCommandService;
import com.github.vvojtas.dailogi_server.dialogue.application.DialogueStatusCommandService;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.CharacterCompleteEventDto;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.DialogueCompleteEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersistenceDialogueEventHandlerTest {

    private static final Long DIALOGUE_ID = 1L;
    private static final Long CHARACTER_ID = 2L;
    private static final int MESSAGE_SEQUENCE_NUMBER = 3;
    private static final String MESSAGE_CONTENT = "Test message content";
    
    @Mock
    private DialogueMessageCommandService messageCommandService;
    
    @Mock
    private DialogueStatusCommandService statusCommandService;
    
    @Captor
    private ArgumentCaptor<DialogueMessageSaveCommand> messageSaveCommandCaptor;
    
    @Captor
    private ArgumentCaptor<Long> dialogueIdCaptor;
    
    @Captor
    private ArgumentCaptor<DialogueStatus> statusCaptor;
    
    private PersistenceDialogueEventHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new PersistenceDialogueEventHandler(
                DIALOGUE_ID,
                messageCommandService,
                statusCommandService
        );
    }
    
    @Test
    void onCharacterComplete_shouldSaveMessage() {
        // Given
        CharacterCompleteEventDto event = new CharacterCompleteEventDto(
                CHARACTER_ID,
                100,
                MESSAGE_CONTENT,
                MESSAGE_SEQUENCE_NUMBER,
                "event-id-123"
        );
        when(messageCommandService.saveMessage(eq(DIALOGUE_ID), any(DialogueMessageSaveCommand.class)))
                .thenReturn(5L); // Return some message ID
        
        // When
        handler.onCharacterComplete(event);
        
        // Then
        verify(messageCommandService).saveMessage(eq(DIALOGUE_ID), messageSaveCommandCaptor.capture());
        DialogueMessageSaveCommand capturedCommand = messageSaveCommandCaptor.getValue();
        assertEquals(CHARACTER_ID, capturedCommand.characterId());
        assertEquals(MESSAGE_CONTENT, capturedCommand.content());
        assertEquals(MESSAGE_SEQUENCE_NUMBER, capturedCommand.messageSequenceNumber());
    }
    
    @Test
    void onDialogueComplete_shouldUpdateStatusToCompleted() {
        // Given
        DialogueCompleteEventDto event = new DialogueCompleteEventDto(
                "completed",
                5,
                "event-id-456"
        );
        when(statusCommandService.updateStatus(any(Long.class), any(DialogueStatus.class)))
                .thenReturn(true);
        
        // When
        handler.onDialogueComplete(event);
        
        // Then
        verify(statusCommandService).updateStatus(dialogueIdCaptor.capture(), statusCaptor.capture());
        assertEquals(DIALOGUE_ID, dialogueIdCaptor.getValue());
        assertEquals(DialogueStatus.COMPLETED, statusCaptor.getValue());
    }
    
    @Test
    void onError_shouldUpdateStatusToFailed() {
        // Given
        Exception exception = new RuntimeException("Test exception");
        when(statusCommandService.updateStatus(any(Long.class), any(DialogueStatus.class)))
                .thenReturn(true);
        
        // When
        handler.onError(DIALOGUE_ID, exception);
        
        // Then
        verify(statusCommandService).updateStatus(dialogueIdCaptor.capture(), statusCaptor.capture());
        assertEquals(DIALOGUE_ID, dialogueIdCaptor.getValue());
        assertEquals(DialogueStatus.FAILED, statusCaptor.getValue());
    }
} 