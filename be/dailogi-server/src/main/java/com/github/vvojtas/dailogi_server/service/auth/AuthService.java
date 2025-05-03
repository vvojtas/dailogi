package com.github.vvojtas.dailogi_server.service.auth;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AppUserRepository;
import com.github.vvojtas.dailogi_server.exception.DuplicateResourceException;
import com.github.vvojtas.dailogi_server.model.auth.mapper.UserMapper;
import com.github.vvojtas.dailogi_server.model.auth.request.LoginCommand;
import com.github.vvojtas.dailogi_server.model.auth.request.RegisterCommand;
import com.github.vvojtas.dailogi_server.model.auth.response.JwtResponseDTO;
import com.github.vvojtas.dailogi_server.model.auth.response.UserDto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String USER_RESOURCE_NAME = AppUser.class.getSimpleName().toLowerCase();
    
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Transactional
    public UserDto register(RegisterCommand request) {
        if (appUserRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(USER_RESOURCE_NAME, "User already exists with name: " + request.name());
        }

        if (!request.password().equals(request.passwordConfirmation())) {
            throw new IllegalArgumentException("Password and confirmation do not match");
        }

        AppUser newUser = AppUser.builder()
                .name(request.name())
                .passwordHash(passwordEncoder.encode(request.password()))
                .isSpecialUser(false)
                .build();

        AppUser savedUser = appUserRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    public JwtResponseDTO authenticate(LoginCommand request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.name(), request.password())
        );

        String jwt = jwtTokenProvider.generateToken(authentication);
        AppUser user = appUserRepository.findByName(request.name())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        return new JwtResponseDTO(
                jwt,
                "Bearer",
                jwtTokenProvider.getExpirationSecondsFromToken(jwt),
                userMapper.toDto(user)
        );
    }
} 