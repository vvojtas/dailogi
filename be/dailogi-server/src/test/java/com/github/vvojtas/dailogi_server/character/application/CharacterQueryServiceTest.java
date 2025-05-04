package com.github.vvojtas.dailogi_server.character.application;

import com.github.vvojtas.dailogi_server.character.api.CharacterQuery;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterListMapper;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterMapper;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterQueryServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private CharacterMapper characterMapper;

    @Mock
    private CharacterListMapper characterListMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CharacterAuthorizationService authorizationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CharacterQueryService queryService;

    private AppUser testUser;
    private Character testCharacter;
    private Pageable pageable;
    private OffsetDateTime now = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1L);

        testCharacter = Character.builder()
                .id(1L)
                .user(testUser)
                .name("Test Character")
                .shortDescription("Short description")
                .description("Full description")
                .isGlobal(false)
                .build();

        pageable = mock(Pageable.class);
    }

    @Test
    @DisplayName("getCharacters should return characters for authenticated user")
    void getCharactersShouldReturnCharactersForAuthenticatedUser() {
        // Arrange
        CharacterQuery query = new CharacterQuery(true, pageable, authentication);
        List<Character> characterList = List.of(testCharacter);
        Page<Character> characterPage = new PageImpl<>(characterList, pageable, 1);
        CharacterListDTO expectedDTO = mock(CharacterListDTO.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(currentUserService.getCurrentAppUser(authentication)).thenReturn(testUser);
        when(characterRepository.findAllByUserAndGlobal(testUser, true, pageable)).thenReturn(characterPage);
        when(characterListMapper.toDTO(characterPage, pageable)).thenReturn(expectedDTO);

        // Act
        CharacterListDTO result = queryService.getCharacters(query);

        // Assert
        assertEquals(expectedDTO, result);
        verify(characterRepository).findAllByUserAndGlobal(testUser, true, pageable);
        verify(characterListMapper).toDTO(characterPage, pageable);
    }

    @Test
    @DisplayName("getCharacters should return only global characters for unauthenticated user")
    void getCharactersShouldReturnOnlyGlobalCharactersForUnauthenticatedUser() {
        // Arrange
        CharacterQuery query = new CharacterQuery(true, pageable, authentication);
        List<Character> characterList = List.of(testCharacter);
        Page<Character> characterPage = new PageImpl<>(characterList, pageable, 1);
        CharacterListDTO expectedDTO = mock(CharacterListDTO.class);

        when(authentication.isAuthenticated()).thenReturn(false);
        when(characterRepository.findAllGlobal(pageable)).thenReturn(characterPage);
        when(characterListMapper.toDTO(characterPage, pageable)).thenReturn(expectedDTO);

        // Act
        CharacterListDTO result = queryService.getCharacters(query);

        // Assert
        assertEquals(expectedDTO, result);
        verify(characterRepository).findAllGlobal(pageable);
        verify(characterListMapper).toDTO(characterPage, pageable);
    }

    @Test
    @DisplayName("getCharacter should return character if accessible")
    void getCharacterShouldReturnCharacterIfAccessible() {
        // Arrange
        CharacterDTO expectedDTO = new CharacterDTO(
            1L,
            "Test Character",
            "Short description",
            "Full description",
            false,
            null,
            false,
            null,
            now,
            null
        );
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(testUser);
        when(authorizationService.canAccess(testCharacter, testUser)).thenReturn(true);
        when(characterMapper.toDTO(testCharacter)).thenReturn(expectedDTO);

        // Act
        CharacterDTO result = queryService.getCharacter(1L);

        // Assert
        assertEquals(expectedDTO, result);
        verify(characterRepository).findById(1L);
        verify(authorizationService).canAccess(testCharacter, testUser);
        verify(characterMapper).toDTO(testCharacter);
    }

    @Test
    @DisplayName("getCharacter should throw ResourceNotFoundException when character not found")
    void getCharacterShouldThrowResourceNotFoundExceptionWhenCharacterNotFound() {
        // Arrange
        when(characterRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> queryService.getCharacter(1L)
        );
        
        assertTrue(exception.getMessage().contains("Character not found"));
    }

    @Test
    @DisplayName("getCharacter should throw AccessDeniedException when user has no access")
    void getCharacterShouldThrowAccessDeniedExceptionWhenUserHasNoAccess() {
        // Arrange
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(testUser);
        when(authorizationService.canAccess(testCharacter, testUser)).thenReturn(false);

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> queryService.getCharacter(1L)
        );
        
        verify(characterRepository).findById(1L);
        verify(authorizationService).canAccess(testCharacter, testUser);
    }
} 