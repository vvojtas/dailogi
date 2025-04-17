package com.github.vvojtas.dailogi_server.model.character.mapper;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class CharacterMapper {

    public CharacterDTO toDTO(Character character) {
        if (character == null) {
            return null;
        }
        
        return new CharacterDTO(
            character.getId(),
            character.getName(),
            character.getShortDescription(),
            character.getDescription(),
            character.getAvatar() != null,
            character.getAvatar() != null ? 
                "data:image/png;base64," + Base64.getEncoder().encodeToString(character.getAvatar()) : 
                null,
            character.getIsGlobal(),
            character.getDefaultLlm() != null ? character.getDefaultLlm().getId() : null,
            character.getCreatedAt(),
            character.getUpdatedAt()
        );
    }
} 