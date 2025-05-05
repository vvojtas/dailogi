package com.github.vvojtas.dailogi_server.db.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "DialogueCharacterConfig")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(of = "id")
public class DialogueCharacterConfig {

    @EmbeddedId
    private DialogueCharacterConfigId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("dialogueId")
    @JoinColumn(name = "dialogue_id", nullable = false)
    private Dialogue dialogue;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("characterId")
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "llm_id", nullable = false)
    private LLM llm;
} 