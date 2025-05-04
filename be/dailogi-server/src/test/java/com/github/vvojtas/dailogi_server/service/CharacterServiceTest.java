package com.github.vvojtas.dailogi_server.service;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.repository.AvatarRepository;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.exception.CharacterLimitExceededException;
import com.github.vvojtas.dailogi_server.exception.DuplicateResourceException;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterListMapper;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterMapper;
import com.github.vvojtas.dailogi_server.model.character.request.AvatarRequest;
import com.github.vvojtas.dailogi_server.model.character.request.CreateCharacterCommand;
import com.github.vvojtas.dailogi_server.model.character.request.UpdateCharacterCommand;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.properties.UserLimitProperties;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.service.util.AvatarUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CharacterServiceTest {

    @Mock
    private UserLimitProperties userLimitProperties;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private LLMRepository llmRepository;

    @Mock
    private CharacterListMapper characterListMapper;

    @Mock
    private CharacterMapper characterMapper;

    @Mock
    private AvatarRepository avatarRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CharacterService characterService;

    private AppUser testUser;
    private Character testCharacter;
    private CharacterDTO testCharacterDTO;
    private LLM testLLM;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1L);

        testLLM = new LLM();
        testLLM.setId(1L);
        testLLM.setName("Test LLM");

        testCharacter = Character.builder()
                .id(1L)
                .user(testUser)
                .name("Test Character")
                .shortDescription("Short description")
                .description("Full description")
                .isGlobal(false)
                .build();

        // Mock CharacterDTO with whatever its actual constructor looks like
        testCharacterDTO = mock(CharacterDTO.class);
        when(testCharacterDTO.id()).thenReturn(1L);
        when(testCharacterDTO.name()).thenReturn("Test Character");
    }

    @Test
    @DisplayName("getCharacters should return characters for authenticated user")
    void getCharactersShouldReturnCharactersForAuthenticatedUser() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        List<Character> characterList = List.of(testCharacter);
        Page<Character> characterPage = new PageImpl<>(characterList, pageable, 1);
        CharacterListDTO expectedDTO = mock(CharacterListDTO.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(currentUserService.getCurrentAppUser(authentication)).thenReturn(testUser);
        when(characterRepository.findAllByUserAndGlobal(testUser, true, pageable)).thenReturn(characterPage);
        when(characterListMapper.toDTO(characterPage, pageable)).thenReturn(expectedDTO);

        // Act
        CharacterListDTO result = characterService.getCharacters(true, pageable, authentication);

        // Assert
        assertEquals(expectedDTO, result);
        verify(characterRepository).findAllByUserAndGlobal(testUser, true, pageable);
        verify(characterListMapper).toDTO(characterPage, pageable);
    }

    @Test
    @DisplayName("getCharacters should return only global characters for unauthenticated user")
    void getCharactersShouldReturnOnlyGlobalCharactersForUnauthenticatedUser() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        List<Character> characterList = List.of(testCharacter);
        Page<Character> characterPage = new PageImpl<>(characterList, pageable, 1);
        CharacterListDTO expectedDTO = mock(CharacterListDTO.class);

        when(authentication.isAuthenticated()).thenReturn(false);
        when(characterRepository.findAllGlobal(pageable)).thenReturn(characterPage);
        when(characterListMapper.toDTO(characterPage, pageable)).thenReturn(expectedDTO);

        // Act
        CharacterListDTO result = characterService.getCharacters(true, pageable, authentication);

        // Assert
        assertEquals(expectedDTO, result);
        verify(characterRepository).findAllGlobal(pageable);
        verify(characterListMapper).toDTO(characterPage, pageable);
    }

    @Test
    @DisplayName("getCharacter should return character if owned or global")
    void getCharacterShouldReturnCharacterIfOwnedOrGlobal() {
        // Arrange
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(testUser);
        when(characterMapper.toDTO(testCharacter)).thenReturn(testCharacterDTO);

        // Act
        CharacterDTO result = characterService.getCharacter(1L);

        // Assert
        assertEquals(testCharacterDTO, result);
        verify(characterRepository).findById(1L);
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
                () -> characterService.getCharacter(1L)
        );
        
        assertTrue(exception.getMessage().contains("Character not found"));
    }

    @Test
    @DisplayName("getCharacter should throw AccessDeniedException when user has no access")
    void getCharacterShouldThrowAccessDeniedExceptionWhenUserHasNoAccess() {
        // Arrange
        AppUser otherUser = new AppUser();
        otherUser.setId(2L);
        
        Character characterForOtherUser = Character.builder()
                .id(1L)
                .user(otherUser)
                .name("Test Character")
                .isGlobal(false)
                .build();
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(characterForOtherUser));
        when(currentUserService.getCurrentAppUserOrNull()).thenReturn(testUser);

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> characterService.getCharacter(1L)
        );
    }

    @Test
    @DisplayName("createCharacter should create and return character")
    void createCharacterShouldCreateAndReturnCharacter() {
        // Arrange
        CreateCharacterCommand command = new CreateCharacterCommand(
            "New Character",
            "Short description",
            "Full description",
            1L,
            null  // No avatar
        );
        
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(characterRepository.countByUser(testUser)).thenReturn(0L);
        when(userLimitProperties.getMaxCharactersPerUser()).thenReturn(50);
        when(characterRepository.existsByNameAndUser(command.name(), testUser)).thenReturn(false);
        when(llmRepository.findById(1L)).thenReturn(Optional.of(testLLM));
        
        // Setup for character saving
        ArgumentCaptor<Character> characterCaptor = ArgumentCaptor.forClass(Character.class);
        when(characterRepository.save(characterCaptor.capture())).thenAnswer(inv -> {
            Character savedCharacter = inv.getArgument(0);
            savedCharacter.setId(1L);
            return savedCharacter;
        });
        
        when(characterMapper.toDTO(any(Character.class))).thenReturn(testCharacterDTO);

        // Act
        CharacterDTO result = characterService.createCharacter(command);

        // Assert
        assertEquals(testCharacterDTO, result);
        
        Character capturedCharacter = characterCaptor.getValue();
        assertEquals(command.name(), capturedCharacter.getName());
        assertEquals(command.shortDescription(), capturedCharacter.getShortDescription());
        assertEquals(command.description(), capturedCharacter.getDescription());
        assertEquals(testLLM, capturedCharacter.getDefaultLlm());
        assertEquals(testUser, capturedCharacter.getUser());
        assertFalse(capturedCharacter.getIsGlobal());
        
        verify(characterRepository).countByUser(testUser);
        verify(characterRepository).existsByNameAndUser(command.name(), testUser);
        verify(llmRepository).findById(1L);
        verify(characterRepository).save(any(Character.class));
        verify(characterMapper).toDTO(any(Character.class));
    }

    @Test
    @DisplayName("createCharacter should create character with avatar")
    void createCharacterShouldCreateCharacterWithAvatar() {
        // Arrange
        String base64Data = Base64.getEncoder().encodeToString("test image data".getBytes());
        
        AvatarRequest avatarRequest = new AvatarRequest(base64Data, "image/png");
        
        CreateCharacterCommand command = new CreateCharacterCommand(
            "New Character",
            "Short description",
            "Full description",
            1L,
            avatarRequest
        );
        
        // Setup necessary service mocks
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(characterRepository.countByUser(testUser)).thenReturn(0L);
        when(userLimitProperties.getMaxCharactersPerUser()).thenReturn(50);
        when(characterRepository.existsByNameAndUser(command.name(), testUser)).thenReturn(false);
        when(llmRepository.findById(1L)).thenReturn(Optional.of(testLLM));
        
        // Mock avatar handling
        Avatar avatar = Avatar.builder()
                .id(1L)
                .data("test image data".getBytes())
                .formatType("image/png")
                .build();
        
        when(avatarRepository.save(any(Avatar.class))).thenReturn(avatar);
        
        // Use Mockito's static mocking for AvatarUtil
        try (MockedStatic<AvatarUtil> mockedStatic = mockStatic(AvatarUtil.class)) {
            // Setup the static mock
            mockedStatic.when(() -> AvatarUtil.validateAndDecodeBase64Avatar(anyString(), anyString()))
                        .thenReturn("test image data".getBytes());
            
            // Setup for character saving
            when(characterRepository.save(any(Character.class))).thenAnswer(inv -> {
                Character savedCharacter = inv.getArgument(0);
                savedCharacter.setId(1L);
                savedCharacter.setAvatar(avatar);
                return savedCharacter;
            });
            
            when(characterMapper.toDTO(any(Character.class))).thenReturn(testCharacterDTO);

            // Act
            CharacterDTO result = characterService.createCharacter(command);

            // Assert
            assertEquals(testCharacterDTO, result);
            
            // Verify the static method was called
            mockedStatic.verify(() -> AvatarUtil.validateAndDecodeBase64Avatar(anyString(), anyString()));
            
            // Verify the repositories were called
            verify(avatarRepository).save(any(Avatar.class));
            verify(characterRepository).save(any(Character.class));
        }
    }

    @Test
    @DisplayName("createCharacter should throw CharacterLimitExceededException when limit reached")
    void createCharacterShouldThrowCharacterLimitExceededExceptionWhenLimitReached() {
        // Arrange
        CreateCharacterCommand command = mock(CreateCharacterCommand.class);
        when(command.name()).thenReturn("New Character");
        
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(characterRepository.countByUser(testUser)).thenReturn(50L);
        when(userLimitProperties.getMaxCharactersPerUser()).thenReturn(50);

        // Act & Assert
        assertThrows(
                CharacterLimitExceededException.class,
                () -> characterService.createCharacter(command)
        );
    }

    @Test
    @DisplayName("createCharacter should throw DuplicateResourceException when name exists")
    void createCharacterShouldThrowDuplicateResourceExceptionWhenNameExists() {
        // Arrange
        CreateCharacterCommand command = mock(CreateCharacterCommand.class);
        when(command.name()).thenReturn("Existing Character");
        
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(characterRepository.countByUser(testUser)).thenReturn(10L);
        when(userLimitProperties.getMaxCharactersPerUser()).thenReturn(50);
        when(characterRepository.existsByNameAndUser(command.name(), testUser)).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> characterService.createCharacter(command)
        );
        
        assertTrue(exception.getMessage().contains("Character with name 'Existing Character' already exists"));
    }

    @Test
    @DisplayName("updateCharacter should update and return character")
    void updateCharacterShouldUpdateAndReturnCharacter() {
        // Arrange
        UpdateCharacterCommand command = new UpdateCharacterCommand(
            "Updated Character",
            "Updated short description",
            "Updated full description",
            2L
        );
        
        LLM newLLM = new LLM();
        newLLM.setId(2L);
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(characterRepository.existsByNameAndUser(command.name(), testUser)).thenReturn(false);
        when(llmRepository.findById(2L)).thenReturn(Optional.of(newLLM));
        when(characterRepository.save(any(Character.class))).thenReturn(testCharacter);
        when(characterMapper.toDTO(testCharacter)).thenReturn(testCharacterDTO);

        // Act
        CharacterDTO result = characterService.updateCharacter(1L, command);

        // Assert
        assertEquals(testCharacterDTO, result);
        
        assertEquals("Updated Character", testCharacter.getName());
        assertEquals("Updated short description", testCharacter.getShortDescription());
        assertEquals("Updated full description", testCharacter.getDescription());
        assertEquals(newLLM, testCharacter.getDefaultLlm());
        
        verify(characterRepository).findById(1L);
        verify(llmRepository).findById(2L);
        verify(characterRepository).save(testCharacter);
        verify(characterMapper).toDTO(testCharacter);
    }

    @Test
    @DisplayName("updateCharacter should throw AccessDeniedException when not owner")
    void updateCharacterShouldThrowAccessDeniedExceptionWhenNotOwner() {
        // Arrange
        UpdateCharacterCommand command = mock(UpdateCharacterCommand.class);
        
        AppUser otherUser = new AppUser();
        otherUser.setId(2L);
        
        Character characterForOtherUser = Character.builder()
                .id(1L)
                .user(otherUser)
                .name("Test Character")
                .isGlobal(false)
                .build();
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(characterForOtherUser));
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);

        // Act & Assert
        assertThrows(
                AccessDeniedException.class,
                () -> characterService.updateCharacter(1L, command)
        );
    }

    @Test
    @DisplayName("deleteCharacter should delete character when all conditions met")
    void deleteCharacterShouldDeleteCharacterWhenAllConditionsMet() {
        // Arrange
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(characterRepository.existsInDialogues(1L)).thenReturn(false);

        // Act
        characterService.deleteCharacter(1L);

        // Assert
        verify(characterRepository).findById(1L);
        verify(currentUserService).getCurrentAppUser();
        verify(characterRepository).existsInDialogues(1L);
        verify(characterRepository).delete(testCharacter);
    }

    @Test
    @DisplayName("deleteCharacter should throw ResponseStatusException when character used in dialogues")
    void deleteCharacterShouldThrowResponseStatusExceptionWhenCharacterUsedInDialogues() {
        // Arrange
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        when(characterRepository.existsInDialogues(1L)).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> characterService.deleteCharacter(1L)
        );
        
        assertTrue(exception.getMessage().contains("Character cannot be deleted because it is used in one or more dialogues"));
    }

    @Test
    @DisplayName("isOwnedOrGlobal should return true when character is owned by user")
    void isOwnedOrGlobalShouldReturnTrueWhenCharacterIsOwnedByUser() {
        // Act
        boolean result = CharacterService.isOwnedOrGlobal(testCharacter, testUser);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isOwnedOrGlobal should return true when character is global")
    void isOwnedOrGlobalShouldReturnTrueWhenCharacterIsGlobal() {
        // Arrange
        AppUser otherUser = new AppUser();
        otherUser.setId(2L);
        
        Character globalCharacter = Character.builder()
                .id(2L)
                .user(otherUser)
                .name("Global Character")
                .isGlobal(true)
                .build();

        // Act
        boolean result = CharacterService.isOwnedOrGlobal(globalCharacter, testUser);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isOwnedOrGlobal should return false when character is neither owned nor global")
    void isOwnedOrGlobalShouldReturnFalseWhenCharacterIsNeitherOwnedNorGlobal() {
        // Arrange
        AppUser otherUser = new AppUser();
        otherUser.setId(2L);
        
        Character otherCharacter = Character.builder()
                .id(2L)
                .user(otherUser)
                .name("Other Character")
                .isGlobal(false)
                .build();

        // Act
        boolean result = CharacterService.isOwnedOrGlobal(otherCharacter, testUser);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isOwned should return true when character is owned by user")
    void isOwnedShouldReturnTrueWhenCharacterIsOwnedByUser() {
        // Act
        boolean result = CharacterService.isOwned(testCharacter, testUser);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isOwned should return false when character is not owned by user")
    void isOwnedShouldReturnFalseWhenCharacterIsNotOwnedByUser() {
        // Arrange
        AppUser otherUser = new AppUser();
        otherUser.setId(2L);
        
        Character otherCharacter = Character.builder()
                .id(2L)
                .user(otherUser)
                .name("Other Character")
                .isGlobal(false)
                .build();

        // Act
        boolean result = CharacterService.isOwned(otherCharacter, testUser);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isGlobal should return true when character is global")
    void isGlobalShouldReturnTrueWhenCharacterIsGlobal() {
        // Arrange
        Character globalCharacter = Character.builder()
                .id(2L)
                .user(testUser)
                .name("Global Character")
                .isGlobal(true)
                .build();

        // Act
        boolean result = CharacterService.isGlobal(globalCharacter);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isGlobal should return false when character is not global")
    void isGlobalShouldReturnFalseWhenCharacterIsNotGlobal() {
        // Act
        boolean result = CharacterService.isGlobal(testCharacter);

        // Assert
        assertFalse(result);
    }
} 