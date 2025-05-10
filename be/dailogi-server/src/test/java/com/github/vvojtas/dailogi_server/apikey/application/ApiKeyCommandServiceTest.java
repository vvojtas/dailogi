package com.github.vvojtas.dailogi_server.apikey.application;

import com.github.vvojtas.dailogi_server.apikey.api.DeleteApiKeyCommand;
import com.github.vvojtas.dailogi_server.apikey.api.SetApiKeyCommand;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AppUserRepository;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.service.util.CryptoService;
import com.github.vvojtas.dailogi_server.service.util.EncryptionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyCommandServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ApiKeyCommandService commandService;

    private AppUser testUser;
    private SetApiKeyCommand setApiKeyCommand;
    private DeleteApiKeyCommand deleteApiKeyCommand;
    private EncryptionResult encryptionResult;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setName("testUser");

        setApiKeyCommand = new SetApiKeyCommand("test-api-key");
        deleteApiKeyCommand = new DeleteApiKeyCommand();
        
        // Test encryption result with dummy values
        encryptionResult = new EncryptionResult("encryptedText", new byte[] {1, 2, 3, 4});
    }

    @Test
    @DisplayName("setApiKey should encrypt and save the API key")
    void setApiKeyShouldEncryptAndSaveApiKey() {
        // Arrange
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(cryptoService.encrypt(anyString())).thenReturn(encryptionResult);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        commandService.setApiKey(setApiKeyCommand);

        // Assert
        verify(currentUserService).getCurrentAppUser();
        verify(cryptoService).encrypt("test-api-key");
        
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        
        AppUser savedUser = userCaptor.getValue();
        assertEquals("encryptedText", savedUser.getEncryptedApiKey());
        assertArrayEquals(new byte[] {1, 2, 3, 4}, savedUser.getApiKeyNonce());
    }

    @Test
    @DisplayName("setApiKey should throw IllegalArgumentException when API key is empty")
    void setApiKeyShouldThrowExceptionWhenApiKeyIsEmpty() {
        // Arrange
        SetApiKeyCommand emptyCommand = new SetApiKeyCommand("");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> commandService.setApiKey(emptyCommand)
        );
        
        assertEquals("API key cannot be null or empty", exception.getMessage());
        verify(currentUserService, never()).getCurrentAppUser();
        verify(cryptoService, never()).encrypt(anyString());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("setApiKey should throw AccessDeniedException when user is not authenticated")
    void setApiKeyShouldThrowExceptionWhenUserNotAuthenticated() {
        // Arrange
        when(currentUserService.getCurrentAppUser()).thenThrow(new AccessDeniedException("No authenticated user"));
        
        // Act & Assert
        assertThrows(
            AccessDeniedException.class,
            () -> commandService.setApiKey(setApiKeyCommand)
        );
        
        verify(currentUserService).getCurrentAppUser();
        verify(cryptoService, never()).encrypt(anyString());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("deleteApiKey should clear API key data and save user")
    void deleteApiKeyShouldClearApiKeyDataAndSaveUser() {
        // Arrange
        testUser.setEncryptedApiKey("existingEncryptedKey");
        testUser.setApiKeyNonce(new byte[] {5, 6, 7, 8});
        
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        commandService.deleteApiKey(deleteApiKeyCommand);

        // Assert
        verify(currentUserService).getCurrentAppUser();
        
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        
        AppUser savedUser = userCaptor.getValue();
        assertNull(savedUser.getEncryptedApiKey());
        assertNull(savedUser.getApiKeyNonce());
    }

    @Test
    @DisplayName("deleteApiKey should throw AccessDeniedException when user is not authenticated")
    void deleteApiKeyShouldThrowExceptionWhenUserNotAuthenticated() {
        // Arrange
        when(currentUserService.getCurrentAppUser()).thenThrow(new AccessDeniedException("No authenticated user"));
        
        // Act & Assert
        assertThrows(
            AccessDeniedException.class,
            () -> commandService.deleteApiKey(deleteApiKeyCommand)
        );
        
        verify(currentUserService).getCurrentAppUser();
        verify(appUserRepository, never()).save(any(AppUser.class));
    }
} 