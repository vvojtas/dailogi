package com.github.vvojtas.dailogi_server.db.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Accessors;
import jakarta.persistence.*;

@Entity
@Table(name = "DialogueMessage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class DialogueMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dialogue_id", nullable = false)
    private Dialogue dialogue;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "character_id", insertable = false, updatable = false)
    private Long characterId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
