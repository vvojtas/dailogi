package com.github.vvojtas.dailogi_server.apikey.application;

import com.github.vvojtas.dailogi_server.apikey.api.ApiKeyStatusQuery;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.model.apikey.response.ApiKeyResponseDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyQueryServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ApiKeyQueryService queryService;

    private AppUser userWithApiKey;
    private AppUser userWithoutApiKey;
    private ApiKeyStatusQuery query;

    @BeforeEach
    void setUp() {
        userWithApiKey = new AppUser();
        userWithApiKey.setId(1L);
        userWithApiKey.setName("userWithKey");
        userWithApiKey.setEncryptedApiKey("encryptedKey");
        userWithApiKey.setApiKeyNonce(new byte[] {1, 2, 3, 4});

        userWithoutApiKey = new AppUser();
        userWithoutApiKey.setId(2L);
        userWithoutApiKey.setName("userWithoutKey");
        
        query = new ApiKeyStatusQuery();
    }

    @Test
    @DisplayName("getApiKeyStatus should return true when user has API key")
    void getApiKeyStatusShouldReturnTrueWhenUserHasApiKey() {
        // Arrange
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(userWithApiKey);

        // Act
        ApiKeyResponseDTO result = queryService.getApiKeyStatus(query);

        // Assert
        assertTrue(result.hasApiKey());
        verify(currentUserService).getCurrentAppUserOrNull();
    }

    @Test
    @DisplayName("getApiKeyStatus should return false when user doesn't have API key")
    void getApiKeyStatusShouldReturnFalseWhenUserDoesNotHaveApiKey() {
        // Arrange
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(userWithoutApiKey);

        // Act
        ApiKeyResponseDTO result = queryService.getApiKeyStatus(query);

        // Assert
        assertFalse(result.hasApiKey());
        verify(currentUserService).getCurrentAppUserOrNull();
    }

    @Test
    @DisplayName("getApiKeyStatus should return false when user has null encrypted key")
    void getApiKeyStatusShouldReturnFalseWhenUserHasNullEncryptedKey() {
        // Arrange
        userWithApiKey.setEncryptedApiKey(null);
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(userWithApiKey);

        // Act
        ApiKeyResponseDTO result = queryService.getApiKeyStatus(query);

        // Assert
        assertFalse(result.hasApiKey());
        verify(currentUserService).getCurrentAppUserOrNull();
    }

    @Test
    @DisplayName("getApiKeyStatus should return false when user has null nonce")
    void getApiKeyStatusShouldReturnFalseWhenUserHasNullNonce() {
        // Arrange
        userWithApiKey.setApiKeyNonce(null);
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(userWithApiKey);

        // Act
        ApiKeyResponseDTO result = queryService.getApiKeyStatus(query);

        // Assert
        assertFalse(result.hasApiKey());
        verify(currentUserService).getCurrentAppUserOrNull();
    }

    @Test
    @DisplayName("getApiKeyStatus should return false when user is not authenticated")
    void getApiKeyStatusShouldReturnFalseWhenUserIsNotAuthenticated() {
        // Arrange
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(null);

        // Act
        ApiKeyResponseDTO result = queryService.getApiKeyStatus(query);

        // Assert
        assertFalse(result.hasApiKey());
        verify(currentUserService).getCurrentAppUserOrNull();
    }
} 