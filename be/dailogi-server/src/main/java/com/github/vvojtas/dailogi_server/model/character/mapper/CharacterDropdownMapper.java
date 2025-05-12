package com.github.vvojtas.dailogi_server.model.character.mapper;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDropdownDTO;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Character entities to CharacterDropdownDTO
 */
@Component
@RequiredArgsConstructor
public class CharacterDropdownMapper {
    
    private final CharacterMapper characterMapper;
    
    /**
     * Maps a Character entity to CharacterDropdownDTO
     * 
     * @param character The source Character entity
     * @return The mapped CharacterDropdownDTO
     */
    public CharacterDropdownDTO toDropdownDTO(Character character) {
        if (character == null) {
            return null;
        }
        
        CharacterDTO dto = characterMapper.toDTO(character);
        return new CharacterDropdownDTO(
            dto.id(),
            dto.name(),
            dto.isGlobal(),
            dto.hasAvatar(),
            dto.avatarUrl()
        );
    }
    
    /**
     * Maps a CharacterDTO to CharacterDropdownDTO
     * 
     * @param characterDTO The source CharacterDTO
     * @return The mapped CharacterDropdownDTO
     */
    public CharacterDropdownDTO fromCharacterDTO(CharacterDTO characterDTO) {
        if (characterDTO == null) {
            return null;
        }
        
        return new CharacterDropdownDTO(
            characterDTO.id(),
            characterDTO.name(),
            characterDTO.isGlobal(),
            characterDTO.hasAvatar(),
            characterDTO.avatarUrl()
        );
    }
    
    /**
     * Maps a list of CharacterDTOs to a list of CharacterDropdownDTOs
     * 
     * @param characterDTOs The source list of CharacterDTOs
     * @return The mapped list of CharacterDropdownDTOs
     */
    public List<CharacterDropdownDTO> toDropdownDTOs(List<CharacterDTO> characterDTOs) {
        if (characterDTOs == null) {
            return null;
        }
        
        return characterDTOs.stream()
            .map(this::fromCharacterDTO)
            .toList();
    }
} 