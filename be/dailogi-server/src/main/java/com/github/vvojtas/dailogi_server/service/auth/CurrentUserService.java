package com.github.vvojtas.dailogi_server.service.auth;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public AppUser getUser() {
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //String name = authentication.getName();
        String name = "Admin"; //TODO: for local development
        return appUserRepository.findByName(name)
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + name));
    }
} 