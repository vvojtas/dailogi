package com.github.vvojtas.dailogi_server.db.repository;

import com.github.vvojtas.dailogi_server.db.entity.DialogueMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DialogueMessageRepository extends JpaRepository<DialogueMessage, Long> {
    // Basic CRUD methods are provided by JpaRepository
} 