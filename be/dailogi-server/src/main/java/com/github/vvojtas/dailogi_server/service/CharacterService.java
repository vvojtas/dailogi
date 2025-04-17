package com.github.vvojtas.dailogi_server.service;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterListMapper;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterListMapper characterListMapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public CharacterListDTO getCharacters(boolean includeGlobal, Pageable pageable) {
        log.debug("Getting characters with includeGlobal={}, pageable={}", includeGlobal, pageable);
        
        Page<Character> characters = characterRepository.findAllByUserAndGlobal(
            currentUser.getUser(),
            includeGlobal,
            pageable
        );
        
        return characterListMapper.toDTO(characters, pageable);
    }
} 