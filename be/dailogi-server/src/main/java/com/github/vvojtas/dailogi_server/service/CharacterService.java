package com.github.vvojtas.dailogi_server.service;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterListMapper;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterListMapper characterListMapper;
    private final CurrentUserService currentUserService;
    private final CharacterMapper characterMapper;

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
        if (character.getIsGlobal()) return true;
        return currentUser.getId().equals(character.getUser().getId());
    }
} 