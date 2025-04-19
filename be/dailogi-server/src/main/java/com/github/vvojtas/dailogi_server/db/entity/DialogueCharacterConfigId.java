package com.github.vvojtas.dailogi_server.db.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DialogueCharacterConfigId implements Serializable {
    private Long dialogueId;
    private Long characterId;
} 