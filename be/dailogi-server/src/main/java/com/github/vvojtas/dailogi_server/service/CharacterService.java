package com.github.vvojtas.dailogi_server.service;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterListMapper;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.properties.UserLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterMapper;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import com.github.vvojtas.dailogi_server.exception.DuplicateResourceException;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.model.character.request.CreateCharacterCommand;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.github.vvojtas.dailogi_server.model.character.request.UpdateCharacterCommand;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterListMapper characterListMapper;
    private final CurrentUserService currentUserService;
    private final CharacterMapper characterMapper;
    private final LLMRepository llmRepository;
    private final UserLimitProperties userLimitProperties;

    @Transactional(readOnly = true)
    public CharacterListDTO getCharacters(boolean includeGlobal, Pageable pageable) {
        log.debug("Getting characters with includeGlobal={}, pageable={}", includeGlobal, pageable);
        
        Page<Character> characters = characterRepository.findAllByUserAndGlobal(
            currentUserService.getUser(),
            includeGlobal,
            pageable
        );
        
        return characterListMapper.toDTO(characters, pageable);
    }

    @Transactional(readOnly = true)
    public CharacterDTO getCharacter(Long id) {
        log.debug("Getting character with id={}", id);
        
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("character", "Character not found with id: " + id));
            
        AppUser currentUser = currentUserService.getUser();
        if (!isOwnedOrGlobal(character, currentUser)) {
            throw new AccessDeniedException("User does not have access to this character");
        }
        
        return characterMapper.toDTO(character);
    }

    private boolean isOwnedOrGlobal(Character character, AppUser currentUser) {
        return isOwned(character, currentUser) || isGlobal(character);
    }

    private boolean isOwned(Character character, AppUser currentUser) {
        return currentUser.getId().equals(character.getUser().getId());
    }

    private boolean isGlobal(Character character) {
        return character.getIsGlobal();
    }

    @Transactional
    public CharacterDTO createCharacter(CreateCharacterCommand command) {
        log.debug("Creating character with name={}", command.name());
        
        AppUser currentUser = currentUserService.getUser();
        
        // Check if user has reached the character limit
        long userCharacterCount = characterRepository.countByUser(currentUser);
        if (userCharacterCount >= userLimitProperties.getMaxCharactersPerUser()) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                String.format("Cannot create more characters. Maximum limit of %d characters reached.", 
                    userLimitProperties.getMaxCharactersPerUser())
            );
        }
        
        Character character = Character.builder()
            .user(currentUser)
            .name(command.name())
            .shortDescription(command.shortDescription())
            .description(command.description())
            .isGlobal(false)
            .build();
        
        if (command.defaultLlmId() != null) {
            LLM defaultLlm = llmRepository.findById(command.defaultLlmId())
                .orElseThrow(() -> new ResourceNotFoundException("llm", "LLM not found with id: " + command.defaultLlmId()));
            character.setDefaultLlm(defaultLlm);
        }
        
        try {
            character = characterRepository.save(character);
            return characterMapper.toDTO(character);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("unique_character_name")) {
                throw new DuplicateResourceException("character", "Character with name '" + command.name() + "' already exists");
            }
            throw e;
        }
    }

    @Transactional
    public CharacterDTO updateCharacter(Long id, UpdateCharacterCommand command) {
        log.debug("Updating character with id={}, name={}", id, command.name());
        
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("character", "Character not found with id: " + id));
            
        AppUser currentUser = currentUserService.getUser();
        if (!isOwned(character, currentUser)) {
            throw new AccessDeniedException("User does not have permission to update this character");
        }
        
        character.setName(command.name())
                .setShortDescription(command.shortDescription())
                .setDescription(command.description());
        
        if (command.defaultLlmId() != null) {
            LLM defaultLlm = llmRepository.findById(command.defaultLlmId())
                .orElseThrow(() -> new ResourceNotFoundException("llm", "LLM not found with id: " + command.defaultLlmId()));
            character.setDefaultLlm(defaultLlm);
        } else {
            character.setDefaultLlm(null);
        }
        
        try {
            character = characterRepository.save(character);
            return characterMapper.toDTO(character);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("unique_character_name")) {
                throw new DuplicateResourceException("character", "Character with name '" + command.name() + "' already exists");
            }
            throw e;
        }
    }
} 