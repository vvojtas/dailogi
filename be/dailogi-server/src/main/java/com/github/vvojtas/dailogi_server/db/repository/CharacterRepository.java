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

    /**
     * Check if a character is referenced in any dialogues
     * @param characterId The ID of the character to check
     * @return true if the character is referenced in any dialogues, false otherwise
     */
    @Query("""
        SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END
        FROM DialogueCharacterConfig dc
        WHERE dc.character.id = :characterId
    """)
    boolean existsInDialogues(@Param("characterId") Long characterId);
    
    /**
     * Check if a character with the given name already exists for the user
     * @param name The character name to check
     * @param user The user who owns the character
     * @return true if a character with the name exists for the user, false otherwise
     */
    boolean existsByNameAndUser(String name, AppUser user);
    
    /**
     * Check if a character with the given name already exists for the user, excluding a specific character
     * @param name The character name to check
     * @param user The user who owns the character
     * @param id The ID of the character to exclude from the check
     * @return true if a character with the name exists for the user (excluding the specified one), false otherwise
     */
    boolean existsByNameAndUserAndIdNot(String name, AppUser user, Long id);
} 