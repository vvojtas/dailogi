package com.github.vvojtas.dailogi_server.model.character.mapper;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CharacterListMapper {

    private final CharacterMapper characterMapper;

    public CharacterListDTO toDTO(Page<Character> characters, Pageable pageable) {
        if (characters == null) {
            return null;
        }
        
        return new CharacterListDTO(
            characters.getContent().stream()
                .map(characterMapper::toDTO)
                .toList(),
            pageable.getPageNumber(),
            pageable.getPageSize(),
            characters.getTotalElements(),
            characters.getTotalPages()
        );
    }
} 