package com.github.vvojtas.dailogi_server.apikey.application;

import com.github.vvojtas.dailogi_server.apikey.api.ApiKeyStatusQuery;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.exception.CryptoException;
import com.github.vvojtas.dailogi_server.model.apikey.response.ApiKeyResponseDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.service.util.CryptoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service for query operations related to API keys
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyQueryService {

    private final CurrentUserService currentUserService;
    private final CryptoService cryptoService;
    
    /**
     * Gets the API key status for the current user
     * 
     * @param query the query parameters
     * @return ApiKeyResponseDTO with status
     */
    @Transactional(readOnly = true)
    public ApiKeyResponseDTO getApiKeyStatus(ApiKeyStatusQuery query) {
        log.debug("Checking API key status for current user");
        boolean hasKey = hasApiKey();
        return new ApiKeyResponseDTO(hasKey);
    }
    
    /**
     * Retrieves and decrypts the API key for the current user
     * 
     * @return The decrypted API key or null if not available
     */
    @Transactional(readOnly = true)
    public String getDecryptedApiKey() {
        log.debug("Retrieving and decrypting API key for current user");
        
        AppUser currentUser = currentUserService.getCurrentAppUserOrNull();
        if (currentUser == null || !hasApiKey()) {
            log.warn("Cannot retrieve API key: user not found or key not set");
            return null;
        }
        
        try {
            String encryptedKey = currentUser.getEncryptedApiKey();
            byte[] keyNonce = currentUser.getApiKeyNonce();
            
            if (!StringUtils.hasText(encryptedKey) || keyNonce == null) {
                log.warn("Cannot retrieve API key: missing encrypted key or nonce");
                return null;
            }
            
            return cryptoService.decrypt(encryptedKey, keyNonce);
        } catch (CryptoException e) {
            log.error("Failed to decrypt API key", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error accessing API key", e);
            return null;
        }
    }
    
    /**
     * Helper method to check if the current user has an API key set
     * 
     * @return true if the user has an API key, false otherwise
     */
    private boolean hasApiKey() {
        AppUser currentUser = currentUserService.getCurrentAppUserOrNull();
        if (currentUser == null) {
            return false;
        }
        
        return StringUtils.hasText(currentUser.getEncryptedApiKey()) && 
               currentUser.getApiKeyNonce() != null;
    }
} 