package com.github.vvojtas.dailogi_server.model.character.mapper;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import org.springframework.stereotype.Component;

@Component
public class CharacterMapper {

    public CharacterDTO toDTO(Character character) {
        if (character == null) {
            return null;
        }

        boolean hasAvatar = character.getAvatarId() != null;
        String avatarUrl = hasAvatar ? "/api/characters/" + character.getId() + "/avatar" : null;

        return new CharacterDTO(
            character.getId(),
            character.getName(),
            character.getShortDescription(),
            character.getDescription(),
            hasAvatar,
            avatarUrl,
            character.getIsGlobal(),
            character.getDefaultLlm() != null ? character.getDefaultLlm().getId() : null,
            character.getCreatedAt(),
            character.getUpdatedAt()
        );
    }
} 