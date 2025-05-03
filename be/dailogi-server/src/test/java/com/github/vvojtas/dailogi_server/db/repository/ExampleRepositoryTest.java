package com.github.vvojtas.dailogi_server.db.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Example repository test using @DataJpaTest to isolate the JPA components
 * and demonstrate repository testing patterns
 */
@DataJpaTest
@ActiveProfiles("test") // Use application-test.yml configuration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Disabled
class ExampleRepositoryTest {

    @Autowired
    private ExampleRepository repository;

    @Test
    @DisplayName("Should save and retrieve entity correctly")
    void shouldSaveAndRetrieveEntityCorrectly() {
        // Arrange
        ExampleEntity entity = new ExampleEntity();
        entity.setName("Test Entity");
        
        // Act
        ExampleEntity savedEntity = repository.save(entity);
        ExampleEntity retrievedEntity = repository.findById(savedEntity.getId()).orElse(null);
        
        // Assert
        assertNotNull(retrievedEntity);
        assertEquals("Test Entity", retrievedEntity.getName());
    }

    @Test
    @DisplayName("Should find entity by name")
    void shouldFindEntityByName() {
        // Arrange
        ExampleEntity entity = new ExampleEntity();
        entity.setName("Unique Name");
        repository.save(entity);
        
        // Act
        ExampleEntity foundEntity = repository.findByName("Unique Name").orElse(null);
        
        // Assert
        assertNotNull(foundEntity);
        assertEquals("Unique Name", foundEntity.getName());
    }

    /**
     * This is a placeholder interface just for the example test
     */
    private interface ExampleRepository {
        ExampleEntity save(ExampleEntity entity);
        java.util.Optional<ExampleEntity> findById(Long id);
        java.util.Optional<ExampleEntity> findByName(String name);
    }

    /**
     * This is a placeholder class just for the example test
     */
    private static class ExampleEntity {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
} 