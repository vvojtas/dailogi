package com.github.vvojtas.dailogi_server.db.repository;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.DialogueCharacterConfig;
import com.github.vvojtas.dailogi_server.db.entity.Dialogue;
import com.github.vvojtas.dailogi_server.db.entity.DialogueCharacterConfigId;
import com.github.vvojtas.dailogi_server.db.entity.DialogueStatus;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Use application-test.yml configuration
class CharacterRepositoryTest {

    @Autowired
    private CharacterRepository characterRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private AppUser user1;
    private AppUser user2;
    private Character character1;
    private Character character2;
    private Character globalCharacter;

    @BeforeEach
    void setUp() {        
        // Create test users
        user1 = AppUser.builder()
                .name("testUser1")
                .passwordHash("hashedPassword1")
                .build();
        
        user2 = AppUser.builder()
                .name("testUser2")
                .passwordHash("hashedPassword2")
                .build();
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        
        // Create test characters
        character1 = Character.builder()
                .name("Character1")
                .description("Description for Character1")
                .shortDescription("Short desc for Character1")
                .user(user1)
                .isGlobal(false)
                .build();
        
        character2 = Character.builder()
                .name("Character2")
                .description("Description for Character2")
                .shortDescription("Short desc for Character2")
                .user(user1)
                .isGlobal(false)
                .build();
        
        globalCharacter = Character.builder()
                .name("GlobalCharacter")
                .description("Description for GlobalCharacter")
                .shortDescription("Short desc for GlobalCharacter")
                .user(user2)
                .isGlobal(true)
                .build();
        
        entityManager.persist(character1);
        entityManager.persist(character2);
        entityManager.persist(globalCharacter);
        
        entityManager.flush();
        entityManager.clear(); // Clear the persistence context to force fresh reads
    }

    @Test
    @DisplayName("Should find characters by user with global characters included")
    void shouldFindAllByUserAndGlobalIncluded() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Character> result = characterRepository.findAllByUserAndGlobal(user1, true, pageable);
        
        // Assert
        assertEquals(3, result.getTotalElements(), "Should return user's characters and global characters");
        assertTrue(result.getContent().stream().anyMatch(c -> c.getName().equals("GlobalCharacter")), 
                "Should include global characters");
    }

    @Test
    @DisplayName("Should find characters by user without global characters")
    void shouldFindAllByUserWithoutGlobal() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Character> result = characterRepository.findAllByUserAndGlobal(user1, false, pageable);
        
        // Assert
        assertEquals(2, result.getTotalElements(), "Should return only user's characters");
        assertFalse(result.getContent().stream().anyMatch(c -> c.getName().equals("GlobalCharacter")), 
                "Should not include global characters");
    }

    @Test
    @DisplayName("Should count characters by user")
    void shouldCountByUser() {
        // Act
        long count = characterRepository.countByUser(user1);
        
        // Assert
        assertEquals(2, count, "Should count only the user's characters");
    }

    @Test
    @DisplayName("Should check if character exists by name and user")
    void shouldCheckIfExistsByNameAndUser() {
        // Act & Assert
        assertTrue(characterRepository.existsByNameAndUser("Character1", user1), 
                "Should return true for existing name and user");
        assertFalse(characterRepository.existsByNameAndUser("NonExistentCharacter", user1), 
                "Should return false for non-existent name");
        assertFalse(characterRepository.existsByNameAndUser("Character1", user2), 
                "Should return false for different user");
    }

    @Test
    @DisplayName("Should check if character exists by name and user excluding specific character")
    void shouldCheckIfExistsByNameAndUserAndIdNot() {
        // Act & Assert
        assertFalse(characterRepository.existsByNameAndUserAndIdNot("Character1", user1, character1.getId()), 
                "Should return false when excluding the character with that name");
        assertTrue(characterRepository.existsByNameAndUserAndIdNot("Character1", user1, character2.getId()), 
                "Should return true when another character with that name exists");
    }

    @Test
    @DisplayName("Should find all global characters")
    void shouldFindAllGlobal() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Character> result = characterRepository.findAllGlobal(pageable);
        
        // Assert
        assertEquals(1, result.getTotalElements(), "Should return only global characters");
        assertEquals("GlobalCharacter", result.getContent().get(0).getName(), 
                "Should return the global character");
    }

    @Test
    @DisplayName("Should check if character exists in dialogues")
    void shouldCheckIfExistsInDialogues() {
        // Arrange - Create fresh entities for this test to avoid detached entity issues
        
        // Create dialogue with managed entities
        Dialogue dialogue = Dialogue.builder()
                .user(user1)
                .name("Test Dialogue")
                .sceneDescription("Test Topic")
                .status(DialogueStatus.COMPLETED)
                .build();
        entityManager.persist(dialogue);
        
        LLM llm = LLM.builder()
                .name("Test LLM")
                .openrouterIdentifier("test-llm")
                .build();
        entityManager.persist(llm);
        
        // First flush to ensure IDs are assigned
        entityManager.flush();
        
        DialogueCharacterConfigId configId = new DialogueCharacterConfigId(
                dialogue.getId(),
                character1.getId()
        );
        
        DialogueCharacterConfig config = DialogueCharacterConfig.builder()
                .id(configId)
                .dialogue(dialogue)
                .character(entityManager.find(Character.class, character1.getId()))
                .llm(llm)
                .build();
        entityManager.persist(config);
        
        entityManager.flush();
        
        // Act & Assert
        assertTrue(characterRepository.existsInDialogues(character1.getId()), 
                "Should return true for character used in a dialogue");
        assertFalse(characterRepository.existsInDialogues(character2.getId()), 
                "Should return false for character not used in any dialogue");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should find characters by user with varying global inclusion")
    void shouldFindAllByUserWithVaryingGlobalInclusion(boolean includeGlobal) {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Character> result = characterRepository.findAllByUserAndGlobal(user1, includeGlobal, pageable);
        
        // Assert
        if (includeGlobal) {
            assertEquals(3, result.getTotalElements(), "Should include global characters when flag is true");
        } else {
            assertEquals(2, result.getTotalElements(), "Should not include global characters when flag is false");
        }
    }
} 