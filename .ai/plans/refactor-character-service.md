# Refactoring Plan: Character Service Component

## Current Analysis

### Issues Identified
1. **High Responsibility Concentration**
   - Single service handling CRUD, validation, authorization, and avatar management
   - Complex transaction boundaries
   - Mixed business and data access logic
   - Static utility methods making testing difficult

2. **Code Smells**
   - Long methods (createCharacter, updateCharacter)
   - Repeated validation patterns
   - Direct repository access across different contexts
   - Static utility methods for business logic
   - Complex error handling mixed with business logic

3. **Architectural Concerns**
   - No clear separation between read and write operations
   - Tight coupling with avatar management
   - Mixed authorization concerns

## Refactoring Strategy

### 1. CQRS Implementation

#### Query Side
```java
@Service
@Transactional(readOnly = true)
public class CharacterQueryService {
    private final CharacterRepository characterRepository;
    private final CharacterMapper characterMapper;
    private final CharacterAuthorizationService authService; // Injected for authorization checks if needed

    public CharacterListDTO getCharacters(CharacterQuery query);
    public CharacterDTO getCharacter(Long id);
}

public record CharacterQuery(
    boolean includeGlobal,
    Pageable pageable,
    Authentication authentication
) {}
```

#### Command Side
```java
@Service
public class CharacterCommandService {
    private final CharacterRepository characterRepository;
    private final CharacterValidator validator;
    private final CharacterAvatarService characterAvatarService; // Direct dependency for avatar operations
    private final LLMRepository llmRepository; // Dependency needed for LLM lookups

    public CharacterDTO createCharacter(CreateCharacterCommand command);
    public CharacterDTO updateCharacter(UpdateCharacterCommand command);
    public void deleteCharacter(DeleteCharacterCommand command);
}
```

### 2. Validation Layer

```java
@Component
public class CharacterValidator {
    private final CharacterRepository characterRepository;
    private final UserLimitProperties properties;
    private final LLMRepository llmRepository; // Needed for LLM existence check

    public void validateForCreation(CreateCharacterCommand command);
    public void validateForUpdate(UpdateCharacterCommand command);
    public void validateForDeletion(Long characterId, AppUser currentUser); // Requires more context
}

public sealed interface ValidationError {
    record DuplicateName(String name) implements ValidationError {}
    record CharacterLimitExceeded(int limit) implements ValidationError {}
    record InvalidLLMReference(Long llmId) implements ValidationError {}
    record CharacterNotFound(Long id) implements ValidationError {}
    record CharacterInUse(Long id) implements ValidationError {} // For deletion validation
}
```

### 3. Authorization Service

```java
@Service
public class CharacterAuthorizationService {
    private final CurrentUserService currentUserService;

    // Methods to check ownership or global status based on Character and Authentication/AppUser
    public void checkCanAccess(Character character, Authentication auth);
    public void checkCanModify(Character character, Authentication auth);
    public void checkCanDelete(Character character, Authentication auth);
    public boolean isOwner(Character character, AppUser user);
    public boolean isGloballyAccessible(Character character);
}
```

### 4. Avatar Management Service (Dedicated)

```java
@Service
public class CharacterAvatarService {
    private final AvatarRepository avatarRepository;
    private final CharacterRepository characterRepository; // Needed to link avatar to character
    private final AvatarUtil avatarUtil; // If validation utils are kept separate

    // Manages avatar persistence linked to a character
    public Optional<Avatar> createAndAttachAvatar(Long characterId, AvatarData avatarData);
    public Optional<Avatar> updateOrAttachAvatar(Long characterId, AvatarData avatarData);
    public void removeAvatar(Long characterId);
}

// Assuming AvatarData is a simple record/DTO for avatar details
public record AvatarData(byte[] data, String contentType) {}
```

## Implementation Phases

### Phase 1: Preparation
1. Create new package structure:
   ```
   com.github.vvojtas.dailogi_server.character/
     ├── api/           # DTOs, Commands, ValidationErrors
     ├── application/   # Services (Query, Command, Validation, Authorization, Avatar)
     ├── domain/        # Character, Avatar (if needed beyond entity), potentially domain exceptions
     ├── infrastructure/# Repositories (Character, Avatar, LLM) and utility integrations
     └── config/        # Configuration (Beans, properties)
   ```
2. Define interfaces/classes for the new services (`CharacterQueryService`, `CharacterCommandService`, `CharacterValidator`, `CharacterAuthorizationService`, `CharacterAvatarService`).
3. Create basic test harnesses for the new components.

### Phase 2: Core Refactoring
1. Implement `CharacterValidator` with detailed validation logic extracted from the old `CharacterService`.
2. Implement `CharacterAuthorizationService` with ownership and access checks, replacing static methods.
3. Implement `CharacterQueryService` for read operations, delegating authorization checks to `CharacterAuthorizationService`.
4. Implement `CharacterCommandService` for write operations (Create, Update, Delete):
   - Inject and use `CharacterValidator` for validation.
   - Inject and use `CharacterAuthorizationService` for permissions.
   - Inject and use `CharacterAvatarService` for avatar operations *directly* within command methods (e.g., `createCharacter`, potentially `updateCharacter`).
   - Handle persistence via `CharacterRepository`.

### Phase 3: Avatar Service Implementation
1. Implement `CharacterAvatarService` focusing *only* on creating, updating, deleting, and linking `Avatar` entities to `Character` entities. Use `AvatarUtil` or include validation logic if preferred.

### Phase 4: Infrastructure & Integration
1. Update repository interfaces (`CharacterRepository`, `AvatarRepository`) if necessary (e.g., add specific query methods).
2. Refine transaction boundaries (`@Transactional`) on the new service methods (Command methods typically require transactions, Query methods read-only).
3. Ensure dependencies (`LLMRepository`, `UserLimitProperties`, `CurrentUserService`) are correctly injected into the new services.

### Phase 5: Migration & Cleanup
1. Create a temporary facade class (`CharacterServiceFacade`) that mimics the old `CharacterService` interface but delegates calls to the new `CharacterQueryService` and `CharacterCommandService`. This allows migrating callers incrementally.
2. Update existing controllers/callers to use the new services directly where possible, or the facade otherwise.
3. Once all callers are migrated, remove the facade and the original `CharacterService`.
4. Update and expand unit and integration tests for the new structure.

## Testing Strategy

1. **Unit Tests**
   - `CharacterValidator`: Test each validation rule in isolation.
   - `CharacterAuthorizationService`: Test permission checks for different scenarios (owner, non-owner, global, anonymous).
   - `CharacterQueryService`: Mock repository and auth service, test data mapping and query logic.
   - `CharacterCommandService`: Mock dependencies (validator, auth, avatar service, repo), test orchestration logic.
   - `CharacterAvatarService`: Mock repositories, test avatar persistence logic.

2. **Integration Tests**
   - Test the full flow for each command (Create, Update, Delete) from `CharacterCommandService` through validation, authorization, persistence, and avatar handling, using a test database (e.g., H2 or Testcontainers).
   - Test query scenarios via `CharacterQueryService` against the test database.
   - Verify transaction behavior (rollbacks on error, etc.).

3. **End-to-End Tests**
   - Test the API endpoints (/api/characters) covering creation, retrieval, update, deletion, including avatar upload/retrieval and authorization rules.

## Risks and Mitigations

1. **Data Consistency during Refactoring**
   - Ensure transaction boundaries correctly encompass all necessary operations within command methods.
   - Rely on integration tests to verify data integrity after operations.

2. **Performance**
   - Monitor query performance, especially in `CharacterQueryService`. Consider caching common queries if needed later.
   - Optimize database interactions (e.g., avoid N+1 queries).

3. **Migration Complexity**
   - Use the Facade pattern (Phase 5, Step 1) to allow gradual migration of dependent components (like controllers).
   - Ensure comprehensive test coverage before removing the old service.

## Success Criteria

1. `CharacterService` class removed or significantly reduced.
2. Clear separation of concerns among new services (`Query`, `Command`, `Validation`, `Authorization`, `Avatar`).
3. Reduced complexity within individual methods.
4. Static utility methods for authorization replaced by `CharacterAuthorizationService`.
5. Improved testability and test coverage (> 80%).
6. Simplified transaction management within focused service methods.
7. Improved maintainability and understandability of the character management feature.
