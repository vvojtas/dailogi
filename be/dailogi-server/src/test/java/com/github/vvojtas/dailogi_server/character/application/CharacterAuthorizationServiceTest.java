package com.github.vvojtas.dailogi_server.character.application;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CharacterAuthorizationServiceTest {

    @InjectMocks
    private CharacterAuthorizationService authService;

    private AppUser ownerUser;
    private AppUser otherUser;
    private Character ownedCharacter;
    private Character otherUserCharacter;
    private Character globalCharacter;

    @BeforeEach
    void setUp() {
        ownerUser = new AppUser();
        ownerUser.setId(1L);
        
        otherUser = new AppUser();
        otherUser.setId(2L);
        
        ownedCharacter = Character.builder()
                .id(1L)
                .user(ownerUser)
                .name("Owned Character")
                .isGlobal(false)
                .build();
                
        otherUserCharacter = Character.builder()
                .id(2L)
                .user(otherUser)
                .name("Other User's Character")
                .isGlobal(false)
                .build();
                
        globalCharacter = Character.builder()
                .id(3L)
                .user(otherUser)
                .name("Global Character")
                .isGlobal(true)
                .build();
    }

    @Test
    @DisplayName("canAccess should return true for owner")
    void canAccessShouldReturnTrueForOwner() {
        // Act
        boolean result = authService.canAccess(ownedCharacter, ownerUser);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("canAccess should return true for global character")
    void canAccessShouldReturnTrueForGlobalCharacter() {
        // Act
        boolean result = authService.canAccess(globalCharacter, ownerUser);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("canAccess should return false for non-owner and non-global character")
    void canAccessShouldReturnFalseForNonOwnerAndNonGlobalCharacter() {
        // Act
        boolean result = authService.canAccess(otherUserCharacter, ownerUser);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("canAccess should return false for null user")
    void canAccessShouldReturnFalseForNullUser() {
        // Act
        boolean result = authService.canAccess(ownedCharacter, null);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("canModify should return true for owner")
    void canModifyShouldReturnTrueForOwner() {
        // Act
        boolean result = authService.canModify(ownedCharacter, ownerUser);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("canModify should return false for non-owner")
    void canModifyShouldReturnFalseForNonOwner() {
        // Act
        boolean result = authService.canModify(otherUserCharacter, ownerUser);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("canModify should return false for global character if not owner")
    void canModifyShouldReturnFalseForGlobalCharacterIfNotOwner() {
        // Act
        boolean result = authService.canModify(globalCharacter, ownerUser);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("canDelete should return true for owner")
    void canDeleteShouldReturnTrueForOwner() {
        // Act
        boolean result = authService.canDelete(ownedCharacter, ownerUser);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("canDelete should return false for non-owner")
    void canDeleteShouldReturnFalseForNonOwner() {
        // Act
        boolean result = authService.canDelete(otherUserCharacter, ownerUser);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("isOwner should return true for owner")
    void isOwnerShouldReturnTrueForOwner() {
        // Act
        boolean result = authService.isOwner(ownedCharacter, ownerUser);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("isOwner should return false for non-owner")
    void isOwnerShouldReturnFalseForNonOwner() {
        // Act
        boolean result = authService.isOwner(ownedCharacter, otherUser);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("isOwner should return false for null user")
    void isOwnerShouldReturnFalseForNullUser() {
        // Act
        boolean result = authService.isOwner(ownedCharacter, null);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("isGloballyAccessible should return true for global character")
    void isGloballyAccessibleShouldReturnTrueForGlobalCharacter() {
        // Act
        boolean result = authService.isGloballyAccessible(globalCharacter);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    @DisplayName("isGloballyAccessible should return false for non-global character")
    void isGloballyAccessibleShouldReturnFalseForNonGlobalCharacter() {
        // Act
        boolean result = authService.isGloballyAccessible(ownedCharacter);
        
        // Assert
        assertFalse(result);
    }
} 