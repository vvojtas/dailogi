package com.github.vvojtas.dailogi_server.db.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "LLM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LLM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "openrouter_identifier", nullable = false, length = 100)
    private String openrouterIdentifier;
} 