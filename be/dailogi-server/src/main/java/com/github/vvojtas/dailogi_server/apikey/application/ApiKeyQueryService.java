package com.github.vvojtas.dailogi_server.apikey.application;

import com.github.vvojtas.dailogi_server.apikey.api.ApiKeyStatusQuery;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.model.apikey.response.ApiKeyResponseDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;

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