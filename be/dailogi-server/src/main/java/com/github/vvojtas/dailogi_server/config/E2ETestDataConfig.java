package com.github.vvojtas.dailogi_server.config;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AppUserRepository;
import com.github.vvojtas.dailogi_server.properties.E2ETestUserProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("e2e-test")
public class E2ETestDataConfig {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final E2ETestUserProperties testUserProperties;

    @Bean
    public CommandLineRunner initializeTestUser() {
        return args -> {
            String testUsername = testUserProperties.getName();
            String testPassword = testUserProperties.getPassword();
            
            if (!appUserRepository.existsByName(testUsername)) {
                log.info("Creating test user '{}' for e2e tests", testUsername);
                AppUser testUser = AppUser.builder()
                        .name(testUsername)
                        .passwordHash(passwordEncoder.encode(testPassword))
                        .isSpecialUser(false)
                        .build();
                
                appUserRepository.save(testUser);
                log.info("Test user created successfully");
            } else {
                log.info("Test user '{}' already exists, skipping creation", testUsername);
            }
        };
    }
} 