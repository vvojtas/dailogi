# Feature: OpenRouter Streaming Integration Plan

## Overview

Implement a real streaming integration with the OpenRouter API for dialogue generation, replacing the mock implementation. Use Spring Boot, WebClient, and SSE to stream tokens as they arrive, following clean code and CQS architecture guidelines.

## High-Level Steps

1. **Define Prompt Builder** (`OpenRouterPromptBuilder`)
   - Create a class responsible for constructing chat messages:
     - Define `record ChatMessage(String role, String content)` in `llm.api` package.
     - Implement `List<ChatMessage> buildDialogueMessages(StreamDialogueCommand command, Map<Long, CharacterDTO> characters, Map<Long, LLMDTO> llms)`.
     - Include system, user, and assistant messages according to OpenRouter API schema.

2. **Extend/OpenRouterInterface**
   - Update `OpenRouterInterface` (`llm.api`) to accept a list of `ChatMessage` instead of a raw prompt string:
     ```java
     UUID streamChat(
       String openRouterIdentifier,
       List<ChatMessage> messages,
       Consumer<String> tokenConsumer,
       Runnable completionListener
     );
     ```
   - Deprecate or remove the old `generateText` after migration.

3. **Configuration Properties**
   - Create `OpenRouterProperties` annotated with `@ConfigurationProperties(prefix = "openrouter.api")`:
     - `String baseUrl = "https://openrouter.ai/api/v1"`
     - (Optional) `Duration connectTimeout`, `Duration readTimeout`
   - Ensure `OPENROUTER_API_KEY` is stored encrypted and retrieved via existing `ApiKeyQueryService` + `CryptoService`.

4. **Configure WebClient Bean**
   - In `config/OpenRouterConfig.java`, define a `@Bean WebClient openRouterWebClient(WebClient.Builder builder, OpenRouterProperties props)`:
     - Set `baseUrl` and default headers (Content-Type: `application/json`, Accept: `text/event-stream`).

5. **Implement Real Client** (`OpenRouterClientImpl`)
   - Annotate with `@Service` and `@RequiredArgsConstructor`.
   - Conditional bean loading:
     - In `OpenRouterMock`, annotate class with `@ConditionalOnProperty(prefix = "openrouter", name = "mock-enabled", havingValue = "true")` so the mock is used when testing.
     - In `OpenRouterClientImpl`, annotate class with `@ConditionalOnProperty(prefix = "openrouter", name = "mock-enabled", havingValue = "false", matchIfMissing = true)` so the real client is used by default.
   - Inject:
     - `WebClient openRouterWebClient`
     - `ApiKeyQueryService apiKeyQueryService`
     - `CryptoService cryptoService`
     - `OpenRouterPromptBuilder promptBuilder`
   - In `streamChat(...)`:
     1. Retrieve encrypted key: `apiKeyQueryService.getApiKeyStatus()`
     2. Decrypt: `cryptoService.decrypt(encryptedKey)`
     3. Build messages: `var messages = promptBuilder.buildDialogueMessages(...)`
     4. Construct request body record `ChatCompletionRequest` with `stream = true`, `model = openRouterIdentifier`, `messages`.
     5. Use `openRouterWebClient.post()` to `/chat/completions`, set Authorization header `Bearer {apiKey}`.
     6. Receive response as `Flux<ServerSentEvent<String>>` or `Flux<String>` via `exchangeToFlux`.
     7. Parse each SSE event's JSON payload to extract `delta.content` and pass to `tokenConsumer.accept(token)`.
     8. On stream completion or `done` signal, invoke `completionListener.run()`.
     - Handle errors by throwing a custom `OpenRouterException` mapped in a `@ControllerAdvice`.

6. **Integrate in Orchestrator**
   - In `DialogueGenerationOrchestrator`, replace `OpenRouterMock` with the new `OpenRouterClientImpl` (via `OpenRouterInterface`).
   - Change calls to use `streamChat` with built messages and listeners.


8. **Error Handling & Validation**
   - Use `@Valid` on `StreamDialogueCommand` in controller.
   - Add guard clauses in service to fail fast on missing API key or empty messages.
   - Extend global `@ControllerAdvice` to handle `OpenRouterException` and return a consistent `ErrorDto`.

9. **Testing Strategy**
   - **Unit Tests** for:
     - `OpenRouterPromptBuilder` message sequences.
     - `OpenRouterClientImpl.streamChat` parsing logic using WireMock to simulate SSE.
   - **Integration Tests**:
     - End-to-end test of `/api/dialogues/stream` using `WebTestClient`, validating SSE events flow.

10. **Documentation & Cleanup**
   - Document new API in Swagger (add docs for `/api/dialogues/stream`).
   - Remove or deprecate `OpenRouterMock` if no longer needed.
   - Update README with instructions on setting `OPENROUTER_API_KEY` and encryption.

---

**Estimated Effort:** 2–3 days of focused development and testing.

**Dependencies:**
- Spring WebFlux (for SSE)
- Jackson (for JSON parsing)
- Existing `CryptoService`, `ApiKeyQueryService`

**Next Steps:**
- Review and approve this plan.
- Begin implementation starting with Prompt Builder and Interface updates. 