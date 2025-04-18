package com.github.vvojtas.dailogi_server.db.repository;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    @Query("""
        SELECT c FROM Character c 
        WHERE c.user = :user 
        OR (c.isGlobal = true AND :includeGlobal = true)
        ORDER BY c.isGlobal ASC, c.name ASC
        """)
    Page<Character> findAllByUserAndGlobal(
        @Param("user") AppUser user,
        @Param("includeGlobal") boolean includeGlobal,
        Pageable pageable
    );

    /**
     * Count the number of characters owned by a user
     * @param user The user to count characters for
     * @return The number of characters owned by the user
     */
    long countByUser(AppUser user);
} 