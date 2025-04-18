package com.github.vvojtas.dailogi_server.service;

import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterListMapper;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.properties.UserLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterMapper;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.exception.DuplicateResourceException;
import com.github.vvojtas.dailogi_server.db.entity.LLM;
import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.model.character.request.CreateCharacterCommand;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.github.vvojtas.dailogi_server.model.character.request.UpdateCharacterCommand;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterAvatarResponseDTO;
import com.github.vvojtas.dailogi_server.model.character.request.UploadAvatarCommand;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final UserLimitProperties userLimitProperties;
    private final CurrentUserService currentUserService;
    private final CharacterRepository characterRepository;
    private final LLMRepository llmRepository;
    private final CharacterListMapper characterListMapper;
    private final CharacterMapper characterMapper;
    
    

    /**
     * Retrieves a paginated list of characters available to the current user.
     * The list includes user's personal characters and optionally global characters.
     * Results are sorted with personal characters first, then global characters, both groups sorted by name.
     * 
     * @param includeGlobal whether to include global characters in the results
     * @param pageable pagination parameters
     * @return paginated list of characters as DTO
     */
    @Transactional(readOnly = true)
    public CharacterListDTO getCharacters(boolean includeGlobal, Pageable pageable) {
        log.debug("Getting characters with includeGlobal={}, pageable={}", includeGlobal, pageable);
        
        AppUser currentUser = currentUserService.getUser();
        log.debug("Fetching characters for user {}", currentUser.getId());
        
        Page<Character> characters = characterRepository.findAllByUserAndGlobal(
            currentUser,
            includeGlobal,
            pageable
        );
        
        log.debug("Found {} characters (total {} in all pages)", 
            characters.getNumberOfElements(), characters.getTotalElements());
            
        log.info("Retrieved {} characters (page {} of {}) for user {}", 
            characters.getNumberOfElements(), 
            pageable.getPageNumber() + 1,
            characters.getTotalPages(),
            currentUser.getId());
        
        return characterListMapper.toDTO(characters, pageable);
    }

    /**
     * Retrieves a single character by its ID.
     * The character must either be owned by the current user or be a global character.
     * 
     * @param id the ID of the character to retrieve
     * @return the character as DTO
     * @throws ResourceNotFoundException if the character does not exist
     * @throws AccessDeniedException if the user does not have access to this character
     */
    @Transactional(readOnly = true)
    public CharacterDTO getCharacter(Long id) {
        log.debug("Getting character with id={}", id);
        
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Attempt to access non-existent character with id={}", id);
                return new ResourceNotFoundException("character", "Character not found with id: " + id);
            });
            
        log.debug("Found character: name={}, isGlobal={}", character.getName(), character.getIsGlobal());
            
        AppUser currentUser = currentUserService.getUser();
        if (!isOwnedOrGlobal(character, currentUser)) {
            log.warn("User {} attempted to access character {} owned by user {}", 
                currentUser.getId(), id, character.getUser().getId());
            throw new AccessDeniedException("User does not have access to this character");
        }
        
        log.info("Retrieved character: id={}, name={}, isGlobal={} for user {}", 
            character.getId(), character.getName(), character.getIsGlobal(), currentUser.getId());
            
        return characterMapper.toDTO(character);
    }

    /**
     * Creates a new character for the current user.
     * The character name must be unique for the user.
     * 
     * @param command the command containing character creation data
     * @return the created character as DTO
     * @throws ResponseStatusException with UNPROCESSABLE_ENTITY if user has reached character limit
     * @throws DuplicateResourceException if character with same name exists for user
     * @throws ResourceNotFoundException if specified LLM does not exist
     */
    @Transactional
    public CharacterDTO createCharacter(CreateCharacterCommand command) {
        log.debug("Creating character with name={}", command.name());
        
        AppUser currentUser = currentUserService.getUser();
        log.debug("Creating character for user {}", currentUser.getId());

        // Check if user has reached the character limit
        long userCharacterCount = characterRepository.countByUser(currentUser);
        if (userCharacterCount >= userLimitProperties.getMaxCharactersPerUser()) {
            log.warn("User {} attempted to exceed character limit of {}", 
                currentUser.getId(), userLimitProperties.getMaxCharactersPerUser());
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                String.format("Cannot create more characters. Maximum limit of %d characters reached.", 
                    userLimitProperties.getMaxCharactersPerUser())
            );
        }
        
        // Verify character name uniqueness for this user
        if (characterRepository.existsByNameAndUser(command.name(), currentUser)) {
            log.warn("User {} attempted to create duplicate character with name '{}'", 
                currentUser.getId(), command.name());
            throw new DuplicateResourceException("character", 
                "Character with name '" + command.name() + "' already exists");
        }
        
        Character character = Character.builder()
            .user(currentUser)
            .name(command.name())
            .shortDescription(command.shortDescription())
            .description(command.description())
            .isGlobal(false)
            .build();
        
        if (command.defaultLlmId() != null) {
            LLM defaultLlm = llmRepository.findById(command.defaultLlmId())
                .orElseThrow(() -> {
                    log.warn("Attempted to set non-existent LLM id={} as default", command.defaultLlmId());
                    return new ResourceNotFoundException("llm", "LLM not found with id: " + command.defaultLlmId());
                });
            character.setDefaultLlm(defaultLlm);
        }
        
        character = characterRepository.save(character);
        log.info("Created new character: id={}, name={}", character.getId(), character.getName());
        
        return characterMapper.toDTO(character);
    }

    /**
     * Updates an existing character with new data.
     * The character must be owned by the current user.
     * The character name must remain unique for the user.
     * 
     * @param id the ID of the character to update
     * @param command the command containing character update data
     * @return the updated character as DTO
     * @throws ResourceNotFoundException if the character or specified LLM does not exist
     * @throws AccessDeniedException if the user does not own the character
     * @throws DuplicateResourceException if character with same name exists for user
     */
    @Transactional
    public CharacterDTO updateCharacter(Long id, UpdateCharacterCommand command) {
        log.debug("Updating character with id={}, name={}", id, command.name());
        
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Attempt to update non-existent character with id={}", id);
                return new ResourceNotFoundException("character", "Character not found with id: " + id);
            });
            
        log.debug("Found character: name={}", character.getName());
            
        AppUser currentUser = currentUserService.getUser();
        if (!isOwned(character, currentUser)) {
            log.warn("User {} attempted to update character {} owned by user {}", 
                currentUser.getId(), id, character.getUser().getId());
            throw new AccessDeniedException("User does not have permission to update this character");
        }
        
        // Verify character name uniqueness for this user (excluding this character)
        if (characterRepository.existsByNameAndUserAndIdNot(command.name(), currentUser, id)) {
            log.warn("User {} attempted to update character {} with duplicate name '{}'", 
                currentUser.getId(), id, command.name());
            throw new DuplicateResourceException("character", 
                "Character with name '" + command.name() + "' already exists");
        }
        
        character.setName(command.name())
                .setShortDescription(command.shortDescription())
                .setDescription(command.description());
        
        if (command.defaultLlmId() != null) {
            LLM defaultLlm = llmRepository.findById(command.defaultLlmId())
                .orElseThrow(() -> {
                    log.warn("Attempted to set non-existent LLM id={} as default", command.defaultLlmId());
                    return new ResourceNotFoundException("llm", "LLM not found with id: " + command.defaultLlmId());
                });
            character.setDefaultLlm(defaultLlm);
        } else {
            character.setDefaultLlm(null);
        }
        
        character = characterRepository.save(character);
        log.info("Updated character: id={}, name={}", character.getId(), character.getName());
        
        return characterMapper.toDTO(character);
    }

    /**
     * Delete a character owned by the current user
     * @param id The ID of the character to delete
     * @throws ResourceNotFoundException if the character does not exist
     * @throws AccessDeniedException if the user does not own the character
     * @throws ResponseStatusException with CONFLICT if the character is used in dialogues
     */
    @Transactional
    public void deleteCharacter(Long id) {
        log.debug("Starting deletion process for character with id={}", id);
        
        // Validate character exists
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Attempt to delete non-existent character with id={}", id);
                return new ResourceNotFoundException("character", "Character not found with id: " + id);
            });
            
        log.debug("Found character: name={}", character.getName());
            
        // Validate ownership
        AppUser currentUser = currentUserService.getUser();
        if (!isOwned(character, currentUser)) {
            log.warn("User {} attempted to delete character {} owned by user {}", 
                currentUser.getId(), id, character.getUser().getId());
            throw new AccessDeniedException("User does not have permission to delete this character");
        }
        
        // Check for dialogue references
        if (characterRepository.existsInDialogues(id)) {
            log.warn("Cannot delete character {} as it is used in dialogues", id);
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Character cannot be deleted because it is used in one or more dialogues"
            );
        }
        
        // Perform deletion
        try {
            log.debug("Deleting character: id={}, name={}", id, character.getName());
            characterRepository.delete(character);
            log.info("Successfully deleted character: id={}, name={}", id, character.getName());
        } catch (Exception e) {
            log.error("Failed to delete character {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while deleting the character"
            );
        }
    }

    /**
     * Checks if a character is owned by a user or is global
     */
    private boolean isOwnedOrGlobal(Character character, AppUser currentUser) {
        return isOwned(character, currentUser) || isGlobal(character);
    }

    /**
     * Checks if a character is owned by a user
     */
    private boolean isOwned(Character character, AppUser currentUser) {
        return currentUser.getId().equals(character.getUser().getId());
    }

    /**
     * Checks if a character is global
     */
    private boolean isGlobal(Character character) {
        return character.getIsGlobal();
    }

    /**
     * Uploads or replaces the avatar for a character.
     * The character must be owned by the current user.
     * Only PNG files up to 1MB and exactly 256x256 pixels are accepted.
     * 
     * @param id the ID of the character to update avatar for
     * @param command the command containing the avatar file
     * @return the updated character avatar information as DTO
          * @throws IOException 
          * @throws ResourceNotFoundException if the character does not exist
          * @throws AccessDeniedException if the user does not own the character
          * @throws ResponseStatusException with BAD_REQUEST if file validation fails
          */
         @Transactional
         public CharacterAvatarResponseDTO uploadAvatar(Long id, UploadAvatarCommand command) {
        log.debug("Starting avatar upload for character with id={}", id);
        
        // Validate character exists and is owned by current user
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Attempt to upload avatar for non-existent character with id={}", id);
                return new ResourceNotFoundException("character", "Character not found with id: " + id);
            });
            
        AppUser currentUser = currentUserService.getUser();
        if (!isOwned(character, currentUser)) {
            log.warn("User {} attempted to upload avatar for character {} owned by user {}", 
                currentUser.getId(), id, character.getUser().getId());
            throw new AccessDeniedException("User does not have permission to update this character's avatar");
        }
        
        MultipartFile file = command.file();
        
        // Validate file type - PNG only
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("image/png")) {
            log.warn("Invalid file type {} for avatar upload", contentType);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only PNG files are allowed"
            );
        }
        
        // Validate file size (1MB = 1048576 bytes)
        if (file.getSize() > 1048576) {
            log.warn("File size {} exceeds maximum allowed size of 1MB", file.getSize());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "File size must not exceed 1MB"
            );
        }
        
        // Read and validate image dimensions
        BufferedImage img;
        try {
            img = ImageIO.read(file.getInputStream());
            if (img == null) {
                log.warn("Failed to read image file");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
            }
            
            if (img.getWidth() != 256 || img.getHeight() != 256) {
                log.warn("Invalid image dimensions: {}x{}", img.getWidth(), img.getHeight());
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image dimensions must be exactly 256x256 pixels"
                );
            }
            
            // Save the avatar
            character.setAvatar(file.getBytes());
            character = characterRepository.save(character);
            
            log.info("Successfully uploaded avatar for character: id={}, name={}", 
                character.getId(), character.getName());
            
            return new CharacterAvatarResponseDTO(
                character.getId(),
                true,
                characterMapper.createAvatarUrl(character.getAvatar())
            );
            
        } catch (IOException e) {
            log.error("Failed to process avatar file: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to process avatar file"
            );
        }
    }
} 