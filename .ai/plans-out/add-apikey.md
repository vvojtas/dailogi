# Plan: Add OpenRouter API Key Support to Backend

1. Modify the initial Flyway migration script `be/dailogi-server/src/main/resources/db/migration/V1__Initial_schema.sql` to include the new columns in the `AppUser` table definition.
   The `CREATE TABLE "AppUser"` statement should be updated to include:
   ```sql
   -- ... other AppUser columns ...
   encrypted_api_key TEXT,
   api_key_nonce BYTEA,
   -- ... other AppUser columns like created_at, updated_at ...
   ```
2. Update the `AppUser` entity (`be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/db/entity/AppUser.java`):
   // ... existing code ...
   FIRST_EDIT
   Add a new field for the encrypted API key:
   ```java
   @Column(name = "encrypted_api_key", columnDefinition = "TEXT")
   private String encryptedApiKey;
   
   @Column(name = "api_key_nonce", columnDefinition = "BYTEA")
   private byte[] apiKeyNonce;
   ```
   // ... existing code ...
3. Update the database plan documentation (`.ai/db-plan.md`):
   // ... existing code ...
   FIRST_EDIT
   In the `User` table section, add:
   - **encrypted_api_key**: TEXT (nullable, stores AES-GCM encrypted OpenRouter API key)
   - **api_key_nonce**: BYTEA (nullable, stores the nonce/IV used for AES-GCM encryption)
   // ... existing code ...
4. Update the `UserDto` record (`be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/model/auth/response/UserDto.java`):
   // ... existing code ...
   FIRST_EDIT
   Add a new boolean property indicating presence of an API key:
   ```java
   @Schema(description = "Indicates if the user has an OpenRouter API key set", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
   @JsonProperty("has_api_key") boolean hasApiKey
   ```
   // ... existing code ...
5. Modify `UserMapper` (`be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/model/auth/mapper/UserMapper.java`):
   // ... existing code ...
   FIRST_EDIT
   Update the `toDto` method to set `hasApiKey` based on `user.getEncryptedApiKey() != null`:
   ```java
   return new UserDto(
       user.getId(),
       user.getName(),
       user.getCreatedAt(),
       user.getEncryptedApiKey() != null
   );
   ```
   // ... existing code ...
6. Update the API plan documentation (`.ai/api-plan.md`):
   // ... existing code ...
   FIRST_EDIT
   Under **API Keys**, add endpoints:
   - **Save OpenRouter API Key**
     - Method: PUT `/api/users/current/api-key`
     - Request: `SetApiKeyCommand` (JSON body `{ "api_key": "string" }`)
     - Response: `APIKeyResponseDTO` with `has_api_key=true`
   - **Delete OpenRouter API Key**
     - Method: DELETE `/api/users/current/api-key`
     - Response: `APIKeyResponseDTO` with `has_api_key=false`
   // ... existing code ...
7. Add encryption key configuration in `application.yml` (`be/dailogi-server/src/main/resources/application.yml`):
   // ... existing code ...
   FIRST_EDIT
   ```yaml
   openrouter:
     encryption:
       key: ${OPENROUTER_ENCRYPTION_KEY}
   ```
   // ... existing code ...
8. Implement a `CryptoService` component (e.g., `be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/service/util/CryptoService.java`):
   - Inject the AES-GCM encryption key (e.g., using `@Value("${openrouter.encryption.key}")`) from application properties. Ensure the key is securely configured and validated (e.g., proper length).
   - Provide methods:
     - `EncryptionResult encrypt(String plaintext)` - returns both cipher text and a securely generated, unique nonce for each encryption.
9. Organize domain-level ApiKey service according to character module structure:
   a. Create a new package `com.github.vvojtas.dailogi_server.apikey.application` under `be/dailogi-server/src/main/java`.
   b. In that package, add `ApiKeyCommandService.java` annotated with `@Service` and `@RequiredArgsConstructor`.
      - Inject `AppUserRepository`, `CryptoService`, and `CurrentUserService`.
      - Implement:
        - `void setApiKey(String apiKey)` — fetch current user via `CurrentUserService`, encrypt and set both `encryptedApiKey` and `apiKeyNonce` on `AppUser`, then save.
        - `void deleteApiKey()` — fetch current user, set both `encryptedApiKey` and `apiKeyNonce` to null, then save.
        - `Optional<String> getDecryptedApiKey()` — if current user has stored key and nonce, decrypt and return the API key
   c. In the same `com.github.vvojtas.dailogi_server.apikey.application` package, add `ApiKeyQueryService.java` implementing `ApiKeyQuery` interface, annotated with `@Service` and `@RequiredArgsConstructor`:
      - Inject `CurrentUserService` (to fetch the current `AppUser`) and `CryptoService`.
      - Implement `boolean hasApiKey()`:
          - Get current `AppUser`.
          - Return `true` if `appUser.getEncryptedApiKey()` is not null and `appUser.getApiKeyNonce()` is not null, otherwise `false`.
      - Implement `Optional<String> getApiKey()`:
          - Get current `AppUser`.
          - If `encryptedApiKey` and `apiKeyNonce` are present, decrypt using `CryptoService` and return `Optional.of(decryptedKey)`.
          - Otherwise, return `Optional.empty()`.
10. Create an `ApiKeyController` in `controller.auth` (`be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/controller/auth/ApiKeyController.java`):
    - Annotate with `@RestController`, `@RequestMapping("/api/users/current/api-key")`, `@RequiredArgsConstructor`, and `@Tag("API Keys")`
    - Define:
      - `@PutMapping` method taking `@Valid @RequestBody SetApiKeyCommand`, calling `apiKeyService.setApiKey(currentUser, command.apiKey())`, returning `APIKeyResponseDTO("API key set successfully", true)`
      - `@DeleteMapping` method calling `apiKeyService.deleteApiKey(currentUser)`, returning `APIKeyResponseDTO("API key deleted successfully", false)`
      - Secure both with `@PreAuthorize("isAuthenticated()")` and OpenAPI annotations
11. Write unit tests:
    - For `CryptoService` to validate encryption/decryption round-trip
    - For `ApiKeyService` to verify setting, deleting, and retrieval of API key
    - For `ApiKeyController` mock tests to ensure endpoints return correct status and payload
12. Run existing and new integration tests against in-memory DB to verify migration, entity mapping, and endpoint behavior. 