# API Endpoint Implementation Plan: Stream Dialogue Generation

## 1. Przegląd punktu końcowego
Rozpoczyna generację dialogu w czasie rzeczywistym i przesyła komunikaty za pomocą Server-Sent Events (SSE). Użytkownik wysyła opis sceny i konfigurację postaci, a serwer emituje strumień zdarzeń token po tokenie.

## 2. Szczegóły żądania
- Metoda HTTP: POST
- Ścieżka: `/api/dialogues/stream`
- Nagłówki:
  - `Authorization: Bearer <token>` (wymagane)
- Request Body (JSON):
  ```json
  {
    "scene_description": "string",          // @NotBlank, max 500 chars
    "character_configs": [                  // @NotEmpty, @Size(min=2, max=3)
      {
        "character_id": 123,               // @NotNull
        "llm_id": 456                      // @NotNull
      }
    ]
  }
  ```

## 3. Wykorzystywane typy
- com.github.vvojtas.dailogi_server.dialogue.api.StartDialogueStreamRequest (record)
- com.github.vvojtas.dailogi_server.dialogue.api.CharacterConfigDto (record)
- com.github.vvojtas.dailogi_server.dialogue.api.StreamDialogueCommand (command)
- Event DTOs:
  - DialogueStartEventDto
  - TokenEventDto
  - CharacterCompleteEventDto
  - DialogueCompleteEventDto
  - ErrorEventDto

## 4. Szczegóły odpowiedzi
- Typ odpowiedzi: `text/event-stream`
- Zdarzenia SSE:
  1. **dialogue-start** (status 200)
     ```json
     {
       "dialogue_id": 1,
       "character_configs": [ ... ],
       "turn_count": 0
     }
     ```
  2. **token**
     ```json
     {
       "character_id": 1,
       "token": "Hello",
       "id": "uuid-token-1"
     }
     ```
  3. **character-complete**
     ```json
     {
       "character_id": 1,
       "token_count": 12,
       "id": "uuid-complete-1"
     }
     ```
  4. **dialogue-complete**
     ```json
     {
       "status": "completed",
       "turn_count": 6,
       "id": "uuid-finish"
     }
     ```
  5. **error**
     ```json
     {
       "message": "string",
       "recoverable": false,
       "id": "uuid-error"
     }
     ```
- Kody stanu:
  - 200 OK (z SSE)
  - 400 Bad Request (walidacja)
  - 401 Unauthorized
  - 402 Payment Required (brak API key)
  - 409 Conflict (limit zasobów lub stan)
  - 500 Internal Server Error

## 5. Przepływ danych
1. Klient wysyła żądanie POST z JSON+Bearer token.
2. Kontroler `DialogueController.streamDialogue(...)`:
   - Waliduje `@Valid StartDialogueStreamRequest`.
   - Tworzy `StreamDialogueCommand` i wywołuje `DialogueStreamService.streamDialogue(cmd)`.
   - Zwraca `SseEmitter` z `MediaType.TEXT_EVENT_STREAM_VALUE`.
3. W `DialogueStreamService` (mock LLM):
   - Tworzy i zapisuje nowy rekord `Dialogue` przez `DialogueRepository`.
   - Emuluje generację tokenów: pętla generuje `TokenEventDto` z opóźnieniem.
   - Po każdej turze wysyła `character-complete`.
   - Po zakończeniu wszystkich tur wysyła `dialogue-complete`.
   - Zapisuje każdą wiadomość w `DialogueMessageRepository`.
4. W razie wyjątku:
   - Loguje błąd (SLF4J) i emituje `error` SSE.
   - Emitter.completeWithError(...)

## 6. Względy bezpieczeństwa
- Uwierzytelnianie: `@PreAuthorize("isAuthenticated()")`.
- Autoryzacja: sprawdzenie własności `Character` przez `CharacterQueryService.getCharacter(id)`.
- Limit jednoczesnych strumieni / timeouty na `SseEmitter.setTimeout(...)`.
- Sanitizacja i walidacja inputu przez Bean Validation.

## 7. Obsługa błędów
- 400: `MethodArgumentNotValidException` -> `400 + ErrorResponseDTO`.
- 401: `AuthenticationException` -> `401`.
- 402: `NoApiKeyException` -> `402`.
- 409: `ResourceConflictException` -> `409`.
- 500: pozostałe `Exception` -> `500`.
- Globalny `@ControllerAdvice` mapuje wyjątki na `ErrorResponseDTO`.

## 8. Wydajność
- Użycie asynchronicznego `SseEmitter` bez blokowania głównego wątku.
- Ograniczenie opóźnień i buforowanie eventów.
- Limit rozmiaru dialogu i tokenów (max 50 tur).

## 9. Kroki implementacji
1. Stworzyć pakiet `dialogue/stream/api` i rekord `StartDialogueStreamRequest` oraz `CharacterConfigDto` z Bean Validation.
2. Stworzyć `StreamDialogueCommand` w `dialogue.stream.api`.
3. Dodać Event DTOs w `dialogue.stream.api`.
4. W `controller` dodać metodę w `DialogueStreamController.java`:
   - `@PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)`
   - `public SseEmitter streamDialogue(@Valid @RequestBody StartDialogueStreamRequest req, Authentication auth)`
5. Stworzyć serwis `DialogueStreamService` (`@Service`) z metodą `SseEmitter streamDialogue(StreamDialogueCommand cmd, Authentication auth)`:
   - Wstrzyknąć `CharacterRepository`, `LLMRepository`, `OpenRouterMock`
   - Implementować zachowanie mock.
6. Stworzyć serwis `OpenRouterMock`
7. Zarejestrować globalny `@ControllerAdvice` dla błędów SSE i walidacji.
8. Dodać wpis w Swagger/OpenAPI (adnotacja `@Operation`).
9. Przegląd kodu i weryfikacja zgodności z wytycznymi spring i Bean Validation.
10. Deploy i test end-to-end z frontendem (mock klient SSE). 