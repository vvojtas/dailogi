package com.github.vvojtas.dailogi_server.service.auth;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AppUserRepository;
import com.github.vvojtas.dailogi_server.model.auth.mapper.UserMapper;
import com.github.vvojtas.dailogi_server.model.auth.response.UserDto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final AppUserRepository appUserRepository;
    private final UserMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(CurrentUserService.class);

    @Transactional(readOnly = true)
    public UserDto getCurrentUserDTO() {
        return userMapper.toDto(getCurrentAppUserOrNull());
    }

    @Transactional(readOnly = true)
    public AppUser getCurrentAppUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Return null if no authentication, not authenticated, or it's the anonymous user token
        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        // Only proceed to find user if genuinely authenticated
        return appUserRepository.findByName(authentication.getName())
                .orElseThrow(() -> {
                    // This should theoretically not happen for a genuinely authenticated user
                    // unless the user was deleted after authentication context was established.
                    log.error("Authenticated user '{}' not found in repository.", authentication.getName());
                    return new IllegalStateException("Authenticated user not found: " + authentication.getName());
                });
    }

    /**
     * Gets the currently authenticated AppUser.
     * Throws AccessDeniedException if the user is not authenticated.
     * @return The authenticated AppUser entity.
     * @throws AccessDeniedException if no user is authenticated.
     */
    @Transactional(readOnly = true)
    public AppUser getCurrentAppUser() {
        AppUser currentUser = getCurrentAppUserOrNull();
        if (currentUser == null) {
            log.warn("Attempted to access current user when not authenticated.");
            throw new AccessDeniedException("User must be authenticated for this operation");
        }
        return currentUser;
    }

    @Transactional(readOnly = true)
    public AppUser getCurrentAppUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        return appUserRepository.findByName(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found: " + authentication.getName()));
    }
} 