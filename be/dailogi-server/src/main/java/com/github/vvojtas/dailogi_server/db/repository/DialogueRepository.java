package com.github.vvojtas.dailogi_server.db.repository;

import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Long> {
    
    /**
     * Count the number of dialogues owned by a user
     * @param user The user to count dialogues for
     * @return The number of dialogues owned by the user
     */
    long countByUser(AppUser user);
} 