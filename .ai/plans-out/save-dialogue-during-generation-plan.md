# Plan Implementacji: Zapisywanie Dialogu Podczas Generowania

**User Story:** US-017 Zapisywanie zakończonego dialogu
**Cel:** Automatyczne zapisywanie generowanego dialogu do bazy danych, w tym jego statusu i poszczególnych wiadomości, oraz obsługa limitu dialogów na użytkownika.

## Faza 1: Modyfikacja Encji i Repozytoriów (jeśli konieczne)

*   **Cel:** Upewnienie się, że encje `Dialogue`, `DialogueMessage`, `DialogueCharacterConfig` oraz `DialogueStatus` są zgodne z wymaganiami.
*   **Kroki:**
    1.  **Przegląd `Dialogue.java`**:
        *   Sprawdzić, czy pole `status` (typu `DialogueStatus`) istnieje i jest poprawnie zmapowane.
        *   Sprawdzić, czy pola `name`, `user`, `sceneDescription`, `createdAt`, `updatedAt` są obecne.
    2.  **Przegląd `DialogueMessage.java`**:
        *   Sprawdzić, czy pola `dialogue` (relacja do `Dialogue`), `character` (relacja do `Character`), `turnNumber` (rozumiane jako numer kolejny wiadomości w dialogu), `content` są obecne.
        *   Upewnić się, że `character_id` jest poprawnie mapowane lub dostępne (np. przez `character.id`).
    3.  **Przegląd `DialogueCharacterConfig.java`**:
        *   Sprawdzić, czy poprawnie mapuje relacje `Dialogue`, `Character`, `LLM`.
    4.  **Przegląd `DialogueStatus.java`**:
        *   Upewnić się, że enum zawiera wartości: `IN_PROGRESS`, `COMPLETED`, `FAILED`.
    5.  **Repozytoria**:
        *   Upewnić się, że istnieją odpowiednie repozytoria Spring Data JPA (`DialogueRepository`, `DialogueMessageRepository`, `DialogueCharacterConfigRepository`) z podstawowymi metodami CRUD.
        *   Do `DialogueRepository` dodać metodę do zliczania dialogów dla danego użytkownika: `long countByUser(AppUser user);` lub `long countByUserId(Long userId);`.

## Faza 2: Rozbudowa Serwisów (Logika Biznesowa)

*   **Cel:** Implementacja logiki biznesowej związanej z tworzeniem, aktualizacją dialogów i wiadomości oraz sprawdzaniem limitów. Te serwisy będą używane przez `PersistenceDialogueEventHandler`.
*   **Kroki:**
    1.  **Rozszerzenie `DialogueCommandService` (lub utworzenie dedykowanego `DialoguePersistenceService`):**
        *   **Modyfikacja `createDialogue(CreateDialogueCommand command, AppUser user)`**: (Wywoływane przez `DialogueStreamService` przed rozpoczęciem generowania)
            *   Upewnić się, że przed utworzeniem dialogu, wywołuje metodę sprawdzającą limit dialogów dla użytkownika. Jeśli limit przekroczony, rzuca odpowiedni wyjątek (np. `DialogueLimitExceededException`).
            *   Ustawia status nowo tworzonej encji `Dialogue` na `DialogueStatus.IN_PROGRESS`.
            *   Poprawnie zapisuje encje `DialogueCharacterConfig` na podstawie `command.characterConfigs()`, wiążąc je z nowo utworzonym `Dialogue`.
            *   Zwraca `DialogueDTO` reprezentujące utworzoną encję.
        *   **Nowa metoda `saveDialogueMessage(long dialogueId, DialogueMessageSaveDTO messageSaveDto)`**: (Wywoływane przez `PersistenceDialogueEventHandler`). `DialogueMessageSaveDTO` powinno zawierać `characterId`, `content` i `messageSequenceNumber` (który zostanie zapisany jako `turnNumber` w encji).
            *   Pobiera encję `Dialogue` po `dialogueId`.
            *   Pobiera encję `Character` po `messageSaveDto.characterId()`.
            *   Tworzy nową encję `DialogueMessage`, ustawiając jej pole `turnNumber` wartością `messageSaveDto.messageSequenceNumber()`, a pozostałe pola (`content`, `character`, `dialogue`) odpowiednio.
            *   Zapisuje encję `DialogueMessage` do bazy.
            *   Oznaczona `@Transactional`.
        *   **Nowa metoda `updateDialogueStatus(long dialogueId, DialogueStatus status)`**: (Wywoływane przez `PersistenceDialogueEventHandler`)
            *   Pobiera encję `Dialogue` po `dialogueId`.
            *   Ustawia jej status na podany.
            *   Zapisuje zmiany w bazie.
            *   Oznaczona `@Transactional`.
    2.  **Rozszerzenie `DialogueQueryService` (lub odpowiedni serwis):**
        *   **Nowa metoda `countDialoguesForUser(AppUser user)` lub `countDialoguesForUser(String username)`**: (Wywoływane przez `DialogueStreamService`)
            *   Wykorzystuje metodę z `DialogueRepository` do zwrócenia liczby dialogów użytkownika.
    3.  **Konfiguracja limitu dialogów:**
        *   Wprowadzić stałą (np. w `application.yml` lub jako `static final int`) określającą maksymalną liczbę dialogów na użytkownika (np. `MAX_DIALOGUES_PER_USER = 50`).

## Faza 3: Modyfikacja Komponentów Strumieniowania i Wprowadzenie Nowych Handlerów

*   **Cel:** Integracja logiki zapisu z procesem strumieniowania SSE przy użyciu dedykowanych `DialogueEventHandler`ów oraz `CompositeDialogueEventHandler`.
*   **Kroki:**
    1.  **Modyfikacja DTO Zdarzeń (np. `CharacterCompleteEventDto`)**:
        *   Dodać pole `messageSequenceNumber` (typu `int` lub `Integer`) do `CharacterCompleteEventDto`. To pole będzie przechowywać numer kolejny wiadomości w całym dialogu.
    2.  **Nowa klasa `PersistenceDialogueEventHandler` implements `DialogueEventHandler`**:
        *   W konstruktorze przyjmuje `DialogueCommandService` (lub `DialoguePersistenceService`) oraz `long dialogueId`.
        *   W metodzie `onCharacterComplete(CharacterCompleteEventDto apiEvent)`:
            *   Pobiera `messageSequenceNumber`, `characterId`, `messageContent` z `apiEvent`.
            *   Tworzy `DialogueMessageSaveDTO` (lub podobne DTO dla serwisu) zawierające te dane.
            *   Wywołuje `dialogueCommandService.saveDialogueMessage(this.dialogueId, dialogueMessageSaveDto)`.
        *   W metodzie `onDialogueComplete(DialogueCompleteEventDto apiEvent)`:
            *   Wywołuje `dialogueCommandService.updateDialogueStatus(this.dialogueId, DialogueStatus.COMPLETED)`.
        *   W metodzie `onError(long dialogueId, Exception exception)`:
            *   Wywołuje `dialogueCommandService.updateDialogueStatus(this.dialogueId, DialogueStatus.FAILED)`.
        *   Metody `onDialogueStart`, `onCharacterStart`, `onToken` mogą mieć puste implementacje.
    3.  **Nowa klasa `CompositeDialogueEventHandler` implements `DialogueEventHandler`**:
        *   W konstruktorze przyjmuje `List<DialogueEventHandler> handlers`.
        *   W każdej metodzie interfejsu (`onDialogueStart`, `onCharacterStart`, `onToken`, `onCharacterComplete`, `onDialogueComplete`, `onError`):
            *   Iteruje po liście `handlers` i wywołuje odpowiednią metodę na każdym z nich.
            *   Rozważyć strategię obsługi wyjątków.
    4.  **Modyfikacja `DialogueStreamService`**:
        *   W metodzie `streamDialogue(StreamDialogueCommand command, Authentication authentication)`:
            *   Po sprawdzeniu limitu dialogów i utworzeniu `dialogueDTO` przez `dialogueCommandService.createDialogue()`:
            *   Utworzyć instancję `SseDialogueEventHandler`.
            *   Utworzyć instancję `PersistenceDialogueEventHandler`.
            *   Utworzyć instancję `CompositeDialogueEventHandler`, przekazując mu listę z `sseEventHandler` i `persistenceEventHandler`.
            *   Przekazać `compositeEventHandler` do `dialogueGenerationOrchestrator.generateDialogue()`.
    5.  **Modyfikacja `DialogueGenerationOrchestrator`**:
        *   W metodzie `handleCharacterCompletion(...)`:
            *   Obliczyć `messageSequenceNumber = messageHistory.size() + 1` (przed dodaniem nowej wiadomości do `messageHistory`).
            *   Podczas tworzenia `CharacterCompleteEventDto`, przekazać obliczony `messageSequenceNumber`.
        *   Nie wymaga innych bezpośrednich zmian w swojej logice, nadal będzie przyjmować jeden `DialogueEventHandler` (instancję `CompositeDialogueEventHandler`).
    6.  **Modyfikacja `SseDialogueEventHandler`**:
        *   Usunąć wstrzyknięcie `DialogueCommandService`.
        *   Upewnić się, że ten handler *nie wykonuje* żadnych operacji zapisu do bazy danych.

## Faza 4: Obsługa Wyjątków i Testowanie

*   **Cel:** Zapewnienie poprawnej obsługi błędów i weryfikacja funkcjonalności.
*   **Kroki:**
    1.  **Globalna obsługa wyjątków:**
        *   Dodać obsługę nowego wyjątku `DialogueLimitExceededException` w globalnym `ControllerAdvice`.
    2.  **Testy jednostkowe:**
        *   Dla nowych/zmienionych metod w serwisach (`DialogueCommandService`, `DialogueQueryService`).
        *   Dla `PersistenceDialogueEventHandler` (mockując `DialogueCommandService`).
        *   Dla `SseDialogueEventHandler` (mockując `SseEmitter`).
        *   Dla `CompositeDialogueEventHandler` (sprawdzając delegację do mockowanych sub-handlerów).
        *   Dla logiki sprawdzania limitu w `DialogueStreamService`.
        *   Dla logiki w `DialogueGenerationOrchestrator` dotyczącej obliczania `messageSequenceNumber` i tworzenia `CharacterCompleteEventDto`.
    3.  **Testy integracyjne:**
        *   Testujące cały przepływ od żądania `/stream` do zapisu w bazie danych (kolejność wiadomości) i wysyłania SSE.
        *   Scenariusz przekroczenia limitu dialogów.
        *   Scenariusz błędu podczas generowania i poprawnej aktualizacji statusu na `FAILED`.
    4.  **Testy manualne (E2E):**
        *   Weryfikacja działania z poziomu interfejsu użytkownika.

## Faza 5: Dokumentacja (jeśli konieczne)

*   **Cel:** Aktualizacja dokumentacji projektowej.
*   **Kroki:**
    1.  Zaktualizować diagramy sekwencji/komponentów, aby odzwierciedlić nowe handlery i przepływ danych (w tym `messageSequenceNumber`).
    2.  Opisać nowe wyjątki i kody błędów API, jeśli dotyczy.

## Rozważenia Dodatkowe:

*   **Nazwa Tymczasowa Dialogu (FR-005, FR-017):** `StreamDialogueCommand` zawiera `dialogueName`. To pole powinno być używane jako nazwa początkowa.
*   **Numer Kolejności Wiadomości (`turnNumber`) w `DialogueMessage`:** Zgodnie z wyjaśnieniem, `turnNumber` w encji `DialogueMessage` oznacza numer kolejny wiadomości w całym dialogu (np. 1, 2, 3...), służący do zachowania kolejności.
    *   W `DialogueGenerationOrchestrator#handleCharacterCompletion`, ten numer kolejny (nazwijmy go `messageSequenceNumber`) powinien być określony jako `messageHistory.size() + 1` (przed dodaniem bieżącej wiadomości do `messageHistory` używanej przez orchestrator).
    *   `CharacterCompleteEventDto` **musi** zostać zmodyfikowane, aby zawierać to pole `messageSequenceNumber`.
    *   `PersistenceDialogueEventHandler` w metodzie `onCharacterComplete` użyje tego `messageSequenceNumber` z `CharacterCompleteEventDto` do stworzenia DTO (np. `DialogueMessageSaveDTO`) przekazywanego do serwisu, który zapisze tę wartość w polu `turnNumber` encji `DialogueMessage`.
*   **Transakcyjność:** Zapewnić, że operacje zapisu w `DialogueCommandService` są transakcyjne (`@Transactional`).

Ten plan dostarcza szczegółowych kroków dla developera w celu zaimplementowania wymaganej funkcjonalności zgodnie z nową architekturą handlerów. 