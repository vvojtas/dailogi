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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterQueryService {
    private static final String CHARACTER_RESOURCE_NAME = Character.class.getSimpleName().toLowerCase();
    
    private final CharacterRepository characterRepository;
    private final CharacterMapper characterMapper;
    private final CharacterListMapper characterListMapper;
    private final CurrentUserService currentUserService;
    private final CharacterAuthorizationService authorizationService;

    /**
     * Retrieves a paginated list of characters available to the current user.
     * The list includes user's personal characters and optionally global characters.
     * Results are sorted with personal characters first, then global characters, both groups sorted by name.
     * 
     * @param query Contains query parameters: includeGlobal, pageable, authentication
     * @return paginated list of characters as DTO
     */
    public CharacterListDTO getCharacters(CharacterQuery query) {
        log.debug("Getting characters with includeGlobal={}, pageable={}, authenticated={}", 
            query.includeGlobal(), query.pageable(), 
            query.authentication() != null && query.authentication().isAuthenticated());

        Page<Character> characters;

        if (query.authentication() != null && query.authentication().isAuthenticated()) {
            // User is authenticated
            AppUser currentUser = currentUserService.getCurrentAppUser(query.authentication());
            log.debug("Fetching characters for authenticated user {}", currentUser.getId());

            characters = characterRepository.findAllByUserAndGlobal(
                currentUser,
                query.includeGlobal(),
                query.pageable()
            );

            log.info("Retrieved {} characters (page {} of {}) for user {}", 
                characters.getNumberOfElements(), 
                query.pageable().getPageNumber() + 1,
                characters.getTotalPages(),
                currentUser.getId());

        } else {
            // User is not authenticated or anonymous
            log.debug("Fetching only global characters for unauthenticated user.");

            // Fetch only global characters, sorted by name
            characters = characterRepository.findAllGlobal(query.pageable()); 
            
            log.info("Retrieved {} global characters (page {} of {}) for unauthenticated user", 
                characters.getNumberOfElements(), 
                query.pageable().getPageNumber() + 1,
                characters.getTotalPages());
        }

        log.debug("Found {} characters (total {} in all pages)", 
                    characters.getNumberOfElements(), characters.getTotalElements());
                    
        return characterListMapper.toDTO(characters, query.pageable());
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
    public CharacterDTO getCharacter(Long id) {
        log.debug("Getting character with id={}", id);
        
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Attempt to access non-existent character with id={}", id);
                return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME, "Character not found with id: " + id);
            });
            
        log.debug("Found character: name={}, isGlobal={}", character.getName(), character.getIsGlobal());
            
        AppUser currentUser = currentUserService.getCurrentAppUserOrNull();
        
        if (!authorizationService.canAccess(character, currentUser)) {
            String currentUserId = (currentUser != null) ? currentUser.getId().toString() : "anonymous";
            log.warn("User {} attempted to access character {} which is not owned or global (Owner: {})", 
                currentUserId, id, character.getUser().getId());
            throw new AccessDeniedException("User does not have access to this character");
        }
        
        String currentUserId = (currentUser != null) ? currentUser.getId().toString() : "anonymous";
        log.info("Retrieved character: id={}, name={}, isGlobal={} for user {}", 
            character.getId(), character.getName(), character.getIsGlobal(), currentUserId);
            
        return characterMapper.toDTO(character);
    }
} 