# Code Analysis and Refactoring Plan

## 1. Code Analysis

### AvatarService Class

**Primary Purpose:** 
Manages avatar operations for characters, including retrieval, upload/update, and deletion of avatar images.

**Dependencies:**
- AvatarRepository
- CharacterRepository
- CurrentUserService
- CharacterAuthorizationService
- AvatarUtil (static utility methods)

**Issues Identified:**

1. **Inconsistency with CQS pattern:**
   ```java
   public class AvatarService {
       // Mixing command and query responsibilities
       public AvatarData getAvatarData(Long characterId, Authentication authentication)
       public void uploadOrUpdateAvatar(Long characterId, UploadAvatarCommand command)
       public void deleteAvatar(Long characterId, Authentication authentication)
   }
   ```
   Unlike the character module that follows Command Query Separation (CQS) pattern with separate CommandService and QueryService classes, the AvatarService mixes both responsibilities.

2. **Inconsistent Authorization handling:**
   ```java
   public AvatarData getAvatarData(Long characterId, Authentication authentication) {
       // Authentication passed as parameter
   }
   
   public void uploadOrUpdateAvatar(Long characterId, UploadAvatarCommand command) {
       // Authentication retrieved from service
       AppUser currentUser = currentUserService.getCurrentAppUser();
   }
   ```
   The authentication handling differs across methods, sometimes relying on injected Authentication, other times using CurrentUserService directly.

3. **Code duplication:**
   ```java
   Character character = characterRepository.findById(characterId)
       .orElseThrow(() -> {
            log.warn("Avatar request failed: Character not found with id={}", characterId);
            return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME, "Character not found with id: " + characterId);
       });
   ```
   This pattern is repeated in multiple methods for character retrieval and validation.

4. **Missing validation abstraction:**
   ```java
   if (file == null || file.isEmpty()) {
       throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is missing or empty.");
   }
   ```
   Validation logic is embedded in the service method rather than in a separate validator component.

5. **Inconsistent error message format:**
   ```java
   throw new ResourceNotFoundException(AVATAR_RESOURCE_NAME, "Character wit id: " + characterId + "has no avatar");
   ```
   Typo in error message and inconsistent spacing in string concatenation.

### LLMService Class

**Primary Purpose:**
Provides query functionality for LLM (Language Learning Model) entities.

**Dependencies:**
- LLMRepository
- LLMMapper

**Issues Identified:**

1. **Lack of CQS pattern implementation:**
   ```java
   public class LLMService {
       @Transactional(readOnly = true)
       public List<LLMDTO> getLLMs() {
           // Only contains query functionality
       }
   }
   ```
   Not structured according to CQS pattern (no separation into api/application packages).

2. **Limited functionality:**
   ```java
   public List<LLMDTO> getLLMs() {
       log.debug("Fetching all LLMs");
       List<LLMDTO> llms = llmMapper.toDTOs(llmRepository.findAll());
       log.info("Found {} LLMs", llms.size());
       return llms;
   }
   ```
   The service is extremely minimal with only one method, suggesting potential future expansion needs.

## 2. Refactoring Plan

### Priority 1: Restructure AvatarService to follow CQS pattern

#### Step 1: Create avatar API package with command and query interfaces

1. Create `be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/avatar/api` package
2. Create command interfaces:

```java
// GetAvatarQuery.java
package com.github.vvojtas.dailogi_server.avatar.api;

import org.springframework.security.core.Authentication;

public record GetAvatarQuery(
    Long characterId,
    Authentication authentication
) {}
```

```java
// DeleteAvatarCommand.java
package com.github.vvojtas.dailogi_server.avatar.api;

import org.springframework.security.core.Authentication;

public record DeleteAvatarCommand(
    Long characterId,
    Authentication authentication
) {}
```

3. Keep the existing `UploadAvatarCommand` class but move it to the new API package

#### Step 2: Create avatar application package with separate query and command services

1. Create `be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/avatar/application` package
2. Create `AvatarQueryService` class:

```java
package com.github.vvojtas.dailogi_server.avatar.application;

import com.github.vvojtas.dailogi_server.avatar.api.GetAvatarQuery;
import com.github.vvojtas.dailogi_server.character.application.CharacterAuthorizationService;
import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AvatarRepository;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.model.avatar.AvatarData;

import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarQueryService {
    private static final String CHARACTER_RESOURCE_NAME = Character.class.getSimpleName().toLowerCase();
    private static final String AVATAR_RESOURCE_NAME = Avatar.class.getSimpleName().toLowerCase();

    private final AvatarRepository avatarRepository;
    private final CharacterRepository characterRepository;
    private final CurrentUserService currentUserService;
    private final CharacterAuthorizationService authorizationService;
    private final AvatarValidator validator;

    public AvatarData getAvatarData(GetAvatarQuery query) {
        log.debug("Attempting to retrieve avatar data for character id={}", query.characterId());
        
        // Validate the character and access authorization
        Character character = validator.validateCharacterExists(query.characterId());
        
        // Validate user has access to character
        AppUser currentUser = currentUserService.getCurrentAppUser(query.authentication());
        validator.validateCharacterAccess(character, currentUser);

        // Check if character has an avatar
        var avatarId = character.getAvatarId();
        if (avatarId == null) {
            log.debug("Character id={} found, but has no avatar (avatarId is null).", query.characterId());
            throw new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                "Character with id: " + query.characterId() + " has no avatar");
        }

        // Fetch the avatar data using avatarId
        log.debug("Fetching avatar id={} for character id={}", avatarId, query.characterId());
        AvatarData avatarData = avatarRepository.findById(avatarId)
            .map(avatar -> new AvatarData(avatar.getData(), avatar.getFormatType()))
            .orElseThrow(() -> {
                 // This case indicates data inconsistency
                 log.error("Data inconsistency: Character {} has avatarId {} but Avatar entity not found.", 
                     query.characterId(), avatarId);
                 return new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                     "Avatar not found with id: " + avatarId);
             });
             
        log.info("Successfully retrieved avatar id={} for character id={}", 
            avatarId, query.characterId());
        return avatarData;
    }
}
```

3. Create `AvatarCommandService` class:

```java
package com.github.vvojtas.dailogi_server.avatar.application;

import com.github.vvojtas.dailogi_server.avatar.api.DeleteAvatarCommand;
import com.github.vvojtas.dailogi_server.avatar.api.UploadAvatarCommand;
import com.github.vvojtas.dailogi_server.character.application.CharacterAuthorizationService;
import com.github.vvojtas.dailogi_server.db.entity.Avatar;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.repository.AvatarRepository;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.service.auth.CurrentUserService;
import com.github.vvojtas.dailogi_server.service.util.AvatarUtil;

import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarCommandService {
    private static final String CHARACTER_RESOURCE_NAME = Character.class.getSimpleName().toLowerCase();
    private static final String AVATAR_RESOURCE_NAME = Avatar.class.getSimpleName().toLowerCase();

    private final AvatarRepository avatarRepository;
    private final CharacterRepository characterRepository;
    private final CurrentUserService currentUserService;
    private final CharacterAuthorizationService authorizationService;
    private final AvatarValidator validator;

    @Transactional
    public void uploadOrUpdateAvatar(Long characterId, UploadAvatarCommand command) throws IOException {
        log.debug("Attempting to upload/update avatar for character id={}", characterId);

        // Validate the command
        validator.validateAvatarUpload(command);
        
        MultipartFile file = command.file();
        String formatType = AvatarUtil.validateAvatarFile(file);
        byte[] avatarData = file.getBytes();

        // Validate character exists and user has permission to modify
        Character character = validator.validateCharacterExists(characterId);
        AppUser currentUser = currentUserService.getCurrentAppUser();
        validator.validateCharacterOwnership(character, currentUser);

        Avatar avatar;
        if (character.getAvatarId() != null) {
            // Update existing avatar
            avatar = avatarRepository.findById(character.getAvatarId())
                .orElseThrow(() -> {
                    log.error("Inconsistency: Character {} has avatarId {} but Avatar entity not found.", 
                        characterId, character.getAvatarId());
                    // If inconsistent, treat as creating a new one
                    return new IllegalStateException("Avatar data inconsistency for character " + characterId);
                });
            avatar.setData(avatarData);
            avatar.setFormatType(formatType);
            log.debug("Updating existing avatar with id={} for character id={}", 
                avatar.getId(), characterId);
        } else {
            // Create new avatar
            avatar = Avatar.builder()
                .data(avatarData)
                .formatType(formatType)
                .build();
            log.debug("Creating new avatar for character id={}", characterId);
        }

        Avatar savedAvatar = avatarRepository.save(avatar);

        // Link avatar to character and save character IF the link has changed
        if (!savedAvatar.equals(character.getAvatar())) {
            character.setAvatar(savedAvatar);
            characterRepository.save(character);
            log.info("Successfully linked avatar id={} to character id={}", 
                savedAvatar.getId(), characterId);
        } else {
            log.info("Successfully updated avatar data for id={} linked to character id={}", 
                savedAvatar.getId(), characterId);
        }
    }

    @Transactional
    public void deleteAvatar(DeleteAvatarCommand command) {
        log.debug("Attempting to delete avatar for character id={}", command.characterId());
        
        // Validate character exists and user has permission to modify
        Character character = validator.validateCharacterExists(command.characterId());
        AppUser currentUser = currentUserService.getCurrentAppUser(command.authentication());
        validator.validateCharacterOwnership(character, currentUser);
        
        // Validate character has an avatar
        if (character.getAvatarId() == null) {
            log.warn("Character id={} has no avatar to delete", command.characterId());
            throw new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                "Character with id " + command.characterId() + " has no avatar");
        }
        
        Long avatarId = character.getAvatarId();
        var avatar = avatarRepository.findById(avatarId)
            .orElseThrow(() -> {
                log.error("Data inconsistency: Character {} has avatarId {} but Avatar entity not found", 
                    command.characterId(), avatarId);
                return new ResourceNotFoundException(AVATAR_RESOURCE_NAME, 
                    "Avatar not found with id: " + avatarId);
            });
        
        avatarRepository.delete(avatar);
        character.setAvatar(null);
        characterRepository.save(character);
        
        log.info("Successfully deleted avatar id={} for character id={}", 
            avatarId, command.characterId());
    }
}
```

#### Step 3: Create AvatarValidator to extract validation logic

```java
package com.github.vvojtas.dailogi_server.avatar.application;

import com.github.vvojtas.dailogi_server.avatar.api.UploadAvatarCommand;
import com.github.vvojtas.dailogi_server.character.application.CharacterAuthorizationService;
import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.db.entity.Character;
import com.github.vvojtas.dailogi_server.db.repository.CharacterRepository;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarValidator {
    private static final String CHARACTER_RESOURCE_NAME = Character.class.getSimpleName().toLowerCase();
    
    private final CharacterRepository characterRepository;
    private final CharacterAuthorizationService authorizationService;
    
    public Character validateCharacterExists(Long characterId) {
        return characterRepository.findById(characterId)
            .orElseThrow(() -> {
                log.warn("Character not found with id={}", characterId);
                return new ResourceNotFoundException(CHARACTER_RESOURCE_NAME, 
                    "Character not found with id: " + characterId);
            });
    }
    
    public void validateCharacterAccess(Character character, AppUser user) {
        if (!authorizationService.canAccess(character, user)) {
            String currentUserId = (user != null) ? user.getId().toString() : "unauthenticated";
            log.warn("Authorization failed: User {} attempted to access avatar for character {} (Owner: {}, Global: {})", 
                currentUserId, character.getId(), 
                character.getUser() != null ? character.getUser().getId() : "null", 
                character.getIsGlobal());
            throw new AccessDeniedException("User does not have access to this character's avatar");
        }
    }
    
    public void validateCharacterOwnership(Character character, AppUser user) {
        if (!authorizationService.canModify(character, user)) {
            log.warn("User {} attempted to modify avatar for character {} owned by user {}",
                user.getId(), character.getId(), 
                character.getUser() != null ? character.getUser().getId() : "null");
            throw new AccessDeniedException("User does not have permission to modify avatar for this character");
        }
    }
    
    public void validateAvatarUpload(UploadAvatarCommand command) {
        if (command.file() == null || command.file().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is missing or empty.");
        }
    }
}
```

#### Step 4: Update AvatarService controller references (Not shown as we don't have controller code)

This will require updating any controllers that currently reference the AvatarService to use the new AvatarQueryService and AvatarCommandService instead.

### Priority 2: Restructure LLMService to follow CQS pattern

#### Step 1: Create llm API package with query interface

```java
package com.github.vvojtas.dailogi_server.llm.api;

public record GetLLMsQuery() {}
```

#### Step 2: Create llm application package with query service

```java
package com.github.vvojtas.dailogi_server.llm.application;

import com.github.vvojtas.dailogi_server.db.repository.LLMRepository;
import com.github.vvojtas.dailogi_server.llm.api.GetLLMsQuery;
import com.github.vvojtas.dailogi_server.model.llm.mapper.LLMMapper;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LLMQueryService {
    private final LLMRepository llmRepository;
    private final LLMMapper llmMapper;

    public List<LLMDTO> getLLMs(GetLLMsQuery query) {
        log.debug("Fetching all LLMs");
        List<LLMDTO> llms = llmMapper.toDTOs(llmRepository.findAll());
        log.info("Found {} LLMs", llms.size());
        return llms;
    }
}
```

#### Step 3: Update LLMService controller references (Not shown as we don't have controller code)

This will require updating any controllers that currently reference the LLMService to use the new LLMQueryService instead.

### Priority 3: Deprecate and eventually remove original services

Once all references to the original services have been updated, mark them as deprecated:

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Deprecated(since = "1.0", forRemoval = true)
public class AvatarService {
    // Add comment explaining migration path
    // @see com.github.vvojtas.dailogi_server.avatar.application.AvatarCommandService
    // @see com.github.vvojtas.dailogi_server.avatar.application.AvatarQueryService
    // ...
}
```

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Deprecated(since = "1.0", forRemoval = true)
public class LLMService {
    // Add comment explaining migration path
    // @see com.github.vvojtas.dailogi_server.llm.application.LLMQueryService
    // ...
}
```

After a suitable transition period, remove the deprecated services entirely from the codebase. 