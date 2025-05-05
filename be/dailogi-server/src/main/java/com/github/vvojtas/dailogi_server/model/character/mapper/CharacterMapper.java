package com.github.vvojtas.dailogi_server.model.character.mapper;

import com.github.vvojtas.dailogi_server.controller.AvatarController;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class CharacterMapper {

    public CharacterDTO toDTO(Character character) {
        if (character == null) {
            return null;
        }

        boolean hasAvatar = character.getAvatarId() != null;
        String avatarUrl = null;
        
        if (hasAvatar) {
            // Use Spring HATEOAS to generate the avatar URL
            avatarUrl = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(AvatarController.class)
                    .getAvatar(character.getId(), null))
                .toUri().toString();
        }

        return new CharacterDTO(
            character.getId(),
            character.getName(),
            character.getShortDescription(),
            character.getDescription(),
            hasAvatar,
            avatarUrl,
            character.getIsGlobal(),
            character.getDefaultLlmId(),
            character.getCreatedAt(),
            character.getUpdatedAt()
        );
    }
} 