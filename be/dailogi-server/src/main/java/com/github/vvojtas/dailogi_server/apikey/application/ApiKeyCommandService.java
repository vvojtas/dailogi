package com.github.vvojtas.dailogi_server.apikey.application;

import com.github.vvojtas.dailogi_server.apikey.api.DeleteApiKeyCommand;
import com.github.vvojtas.dailogi_server.apikey.api.SetApiKeyCommand;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AppUserRepository;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.service.util.CryptoService;
import com.github.vvojtas.dailogi_server.service.util.EncryptionResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service for command operations related to API keys
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyCommandService {

    private final AppUserRepository appUserRepository;
    private final CryptoService cryptoService;
    private final CurrentUserService currentUserService;
    
    /**
     * Sets the API key for the current user
     * 
     * @param command command containing the API key to set
     * @throws IllegalArgumentException if the API key is null or empty
     * @throws AccessDeniedException if no user is authenticated
     */
    @Transactional
    public void setApiKey(SetApiKeyCommand command) {
        if (!StringUtils.hasText(command.apiKey())) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        
        AppUser currentUser = getCurrentUser();
        
        // Encrypt the API key
        EncryptionResult encryptionResult = cryptoService.encrypt(command.apiKey());
        
        // Update the user's API key data
        currentUser.setEncryptedApiKey(encryptionResult.getCipherText());
        currentUser.setApiKeyNonce(encryptionResult.getNonce());
        
        // Save the changes
        appUserRepository.save(currentUser);
        
        log.info("API key set for user: {}", currentUser.getName());
    }
    
    /**
     * Deletes the API key for the current user
     * 
     * @param command the deletion command (empty)
     * @throws AccessDeniedException if no user is authenticated
     */
    @Transactional
    public void deleteApiKey(DeleteApiKeyCommand command) {
        AppUser currentUser = getCurrentUser();
        
        // Clear the API key data
        currentUser.setEncryptedApiKey(null);
        currentUser.setApiKeyNonce(null);
        
        // Save the changes
        appUserRepository.save(currentUser);
        
        log.info("API key deleted for user: {}", currentUser.getName());
    }
    
    /**
     * Helper method to get the current authenticated user
     * 
     * @return the current user
     * @throws AccessDeniedException if no user is authenticated
     */
    private AppUser getCurrentUser() {
        AppUser currentUser = currentUserService.getCurrentAppUser();
        if (currentUser == null) {
            throw new AccessDeniedException("No authenticated user found");
        }
        return currentUser;
    }
} 