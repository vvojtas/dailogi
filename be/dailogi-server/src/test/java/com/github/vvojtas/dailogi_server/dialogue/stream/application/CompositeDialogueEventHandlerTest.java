package com.github.vvojtas.dailogi_server.dialogue.stream.application;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.DialogueEventHandler;
import com.github.vvojtas.dailogi_server.dialogue.stream.api.event.*;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompositeDialogueEventHandlerTest {

    private static final Long DIALOGUE_ID = 1L;
    
    @Mock
    private DialogueEventHandler handler1;
    
    @Mock
    private DialogueEventHandler handler2;
    
    private CompositeDialogueEventHandler compositeHandler;
    
    @BeforeEach
    void setUp() {
        compositeHandler = new CompositeDialogueEventHandler(DIALOGUE_ID, Arrays.asList(handler1, handler2));
    }
    
    @Test
    void onDialogueStart_shouldDelegateToAllHandlers() {
        // Given
        DialogueStartEventDto event = new DialogueStartEventDto(
                DIALOGUE_ID,
                List.of(new CharacterConfigDTO(1L, 2L)),
                5
        );
        
        // When
        compositeHandler.onDialogueStart(event);
        
        // Then
        verify(handler1).onDialogueStart(event);
        verify(handler2).onDialogueStart(event);
    }
    
    @Test
    void onCharacterStart_shouldDelegateToAllHandlers() {
        // Given
        CharacterConfigDTO config = new CharacterConfigDTO(1L, 2L);
        CharacterStartEventDto event = new CharacterStartEventDto(config, "event-id-123");
        
        // When
        compositeHandler.onCharacterStart(event);
        
        // Then
        verify(handler1).onCharacterStart(event);
        verify(handler2).onCharacterStart(event);
    }
    
    @Test
    void onToken_shouldDelegateToAllHandlers() {
        // Given
        CharacterConfigDTO config = new CharacterConfigDTO(1L, 2L);
        TokenEventDto event = new TokenEventDto(config, "token", "event-id-123");
        
        // When
        compositeHandler.onToken(event);
        
        // Then
        verify(handler1).onToken(event);
        verify(handler2).onToken(event);
    }
    
    @Test
    void onCharacterComplete_shouldDelegateToAllHandlers() {
        // Given
        CharacterCompleteEventDto event = new CharacterCompleteEventDto(
                1L,
                100,
                "message content",
                3,
                "event-id-123"
        );
        
        // When
        compositeHandler.onCharacterComplete(event);
        
        // Then
        verify(handler1).onCharacterComplete(event);
        verify(handler2).onCharacterComplete(event);
    }
    
    @Test
    void onDialogueComplete_shouldDelegateToAllHandlers() {
        // Given
        DialogueCompleteEventDto event = new DialogueCompleteEventDto(
                "completed",
                5,
                "event-id-123"
        );
        
        // When
        compositeHandler.onDialogueComplete(event);
        
        // Then
        verify(handler1).onDialogueComplete(event);
        verify(handler2).onDialogueComplete(event);
    }
    
    @Test
    void onError_shouldDelegateToAllHandlers() {
        // Given
        Exception exception = new RuntimeException("Test exception");
        
        // When
        compositeHandler.onError(DIALOGUE_ID, exception);
        
        // Then
        verify(handler1).onError(DIALOGUE_ID, exception);
        verify(handler2).onError(DIALOGUE_ID, exception);
    }
    
    @Test
    void shouldContinueToSecondHandlerEvenIfFirstHandlerThrowsException() {
        // Given
        CharacterCompleteEventDto event = new CharacterCompleteEventDto(
                1L,
                100,
                "message content",
                3,
                "event-id-123"
        );
        doThrow(new RuntimeException("Handler error")).when(handler1).onCharacterComplete(any());
        
        // When
        compositeHandler.onCharacterComplete(event);
        
        // Then - handler2 should still be called despite handler1 throwing an exception
        verify(handler2).onCharacterComplete(event);
    }
    
    @Test
    void shouldHandleAccessDeniedException() {
        // Given
        CharacterCompleteEventDto event = new CharacterCompleteEventDto(
                1L,
                100,
                "message content",
                3,
                "event-id-123"
        );
        doThrow(new AccessDeniedException("Not authorized")).when(handler1).onCharacterComplete(any());
        
        // When
        compositeHandler.onCharacterComplete(event);
        
        // Then - handler2 should still be called despite handler1 throwing AccessDeniedException
        verify(handler2).onCharacterComplete(event);
    }
} 