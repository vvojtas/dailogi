package com.github.vvojtas.dailogi_server.db.repository;

import com.github.vvojtas.dailogi_server.db.entity.DialogueCharacterConfig;
import com.github.vvojtas.dailogi_server.db.entity.DialogueCharacterConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DialogueCharacterConfigRepository extends JpaRepository<DialogueCharacterConfig, DialogueCharacterConfigId> {
} 