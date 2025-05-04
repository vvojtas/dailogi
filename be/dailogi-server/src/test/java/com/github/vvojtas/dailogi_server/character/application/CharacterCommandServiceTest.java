package com.github.vvojtas.dailogi_server.character.application;

import com.github.vvojtas.dailogi_server.character.api.CreateCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.DeleteCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.UpdateCharacterCommand;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterMapper;
import com.github.vvojtas.dailogi_server.model.character.request.AvatarRequest;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterCommandServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private LLMRepository llmRepository;

    @Mock
    private CharacterValidator validator;

    @Mock
    private CharacterAvatarService avatarService;

    @Mock
    private CharacterMapper characterMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CharacterCommandService commandService;

    private AppUser testUser;
    private Character testCharacter;
    private LLM testLLM;
    private CreateCharacterCommand createCommand;
    private UpdateCharacterCommand updateCommand;
    private DeleteCharacterCommand deleteCommand;
    private AvatarRequest avatarRequest;
    private OffsetDateTime now = OffsetDateTime.now();

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

        avatarRequest = new AvatarRequest("base64data", "image/png");
        
        createCommand = new CreateCharacterCommand(
            "New Character",
            "Short description",
            "Full description",
            1L,
            avatarRequest
        );
        
        updateCommand = new UpdateCharacterCommand(
            1L,
            "Updated Character",
            "Updated short description",
            "Updated full description",
            2L,
            avatarRequest
        );
        
        deleteCommand = new DeleteCharacterCommand(1L);
    }

    @Test
    @DisplayName("createCharacter should create and return character")
    void createCharacterShouldCreateAndReturnCharacter() {
        // Arrange
        CharacterDTO expectedDTO = new CharacterDTO(
            1L,
            "Test Character",
            "Short description",
            "Full description",
            false,
            null,
            false,
            1L,
            now,
            null
        );
        
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        doNothing().when(validator).validateForCreation(createCommand, testUser);
        when(llmRepository.findById(1L)).thenReturn(Optional.of(testLLM));
        
        // Setup for character saving
        ArgumentCaptor<Character> characterCaptor = ArgumentCaptor.forClass(Character.class);
        when(characterRepository.save(characterCaptor.capture())).thenAnswer(inv -> {
            Character savedCharacter = inv.getArgument(0);
            savedCharacter.setId(1L);
            return savedCharacter;
        });
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(characterMapper.toDTO(any(Character.class))).thenReturn(expectedDTO);

        // Act
        CharacterDTO result = commandService.createCharacter(createCommand);

        // Assert
        assertEquals(expectedDTO, result);
        
        Character capturedCharacter = characterCaptor.getValue();
        assertEquals(createCommand.name(), capturedCharacter.getName());
        assertEquals(createCommand.shortDescription(), capturedCharacter.getShortDescription());
        assertEquals(createCommand.description(), capturedCharacter.getDescription());
        assertEquals(testLLM, capturedCharacter.getDefaultLlm());
        assertEquals(testUser, capturedCharacter.getUser());
        assertFalse(capturedCharacter.getIsGlobal());
        
        verify(currentUserService).getCurrentAppUser();
        verify(validator).validateForCreation(createCommand, testUser);
        verify(llmRepository).findById(1L);
        verify(characterRepository).save(any(Character.class));
        verify(avatarService).createAndAttachAvatar(anyLong(), eq(avatarRequest));
        verify(characterMapper).toDTO(any(Character.class));
    }

    @Test
    @DisplayName("updateCharacter should update and return character")
    void updateCharacterShouldUpdateAndReturnCharacter() {
        // Arrange
        CharacterDTO expectedDTO = new CharacterDTO(
            1L,
            "Test Character",
            "Short description",
            "Full description",
            false,
            null,
            false,
            1L,
            now,
            now
        );
        
        LLM newLLM = new LLM();
        newLLM.setId(2L);
        
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        doNothing().when(validator).validateForUpdate(updateCommand, testUser);
        when(validator.getCharacterById(1L)).thenReturn(testCharacter);
        when(llmRepository.findById(2L)).thenReturn(Optional.of(newLLM));
        when(characterRepository.save(any(Character.class))).thenReturn(testCharacter);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(testCharacter));
        when(characterMapper.toDTO(testCharacter)).thenReturn(expectedDTO);

        // Act
        CharacterDTO result = commandService.updateCharacter(updateCommand);

        // Assert
        assertEquals(expectedDTO, result);
        
        verify(currentUserService).getCurrentAppUser();
        verify(validator).validateForUpdate(updateCommand, testUser);
        verify(validator).getCharacterById(1L);
        verify(llmRepository).findById(2L);
        verify(characterRepository).save(testCharacter);
        verify(avatarService).updateOrAttachAvatar(anyLong(), eq(avatarRequest));
        verify(characterMapper).toDTO(testCharacter);
    }

    @Test
    @DisplayName("deleteCharacter should delete the character")
    void deleteCharacterShouldDeleteTheCharacter() {
        // Arrange
        when(currentUserService.getCurrentAppUser()).thenReturn(testUser);
        doNothing().when(validator).validateForDeletion(deleteCommand, testUser);
        when(validator.getCharacterById(1L)).thenReturn(testCharacter);

        // Act
        commandService.deleteCharacter(deleteCommand);

        // Assert
        verify(currentUserService).getCurrentAppUser();
        verify(validator).validateForDeletion(deleteCommand, testUser);
        verify(validator).getCharacterById(1L);
        verify(characterRepository).delete(testCharacter);
    }
} 