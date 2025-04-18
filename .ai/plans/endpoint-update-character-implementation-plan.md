# API Endpoint Implementation Plan: Update Character

## 1. Przegląd punktu końcowego

Endpoint służy do aktualizacji danych istniejącej postaci (character). Umożliwia użytkownikowi modyfikację takich pól jak nazwa, krótki opis, opis oraz opcjonalnie domyślne ustawienie identyfikatora LLM. Endpoint ten jest częścią zabezpieczonego API, w którym autentykacja i autoryzacja mają kluczowe znaczenie.

## 2. Szczegóły żądania

- **Metoda HTTP:** PUT
- **URL:** `/api/characters/{id}`
- **Parametry ścieżki:**
  - `id` (wymagany, typ: long) – identyfikator postaci do aktualizacji
- **Request Body:**
  ```json
  {
    "name": "string",
    "short_description": "string",
    "description": "string",
    "default_llm_id": "long (optional)"
  }
  ```

### Wymagane parametry

- `name`: nowa nazwa postaci
- `short_description`: nowy krótki opis
- `description`: nowy opis

### Opcjonalne parametry

- `default_llm_id`: identyfikator domyślnego LLM, może być pominięty lub ustawiony na null

## 3. Wykorzystywane typy

- **Command DTO:** `UpdateCharacterCommand` – zawiera: `name`, `short_description`, `description`, `default_llm_id`.
- **Response DTO:** Należy utworzyć (jeśli nie istnieje) obiekt odpowiedzi zawierający:
  - `id` (long)
  - `name` (string)
  - `short_description` (string)
  - `description` (string)
  - `has_avatar` (boolean)
  - `avatar_url` (string)
  - `is_global` (boolean)
  - `default_llm_id` (long)
  - `created_at` (timestamp)
  - `updated_at` (timestamp)

## 4. Szczegóły odpowiedzi

- **Kod odpowiedzi:** 200 OK w przypadku sukcesu
- **Struktura odpowiedzi:**
  ```json
  {
    "id": "long",
    "name": "string",
    "short_description": "string",
    "description": "string",
    "has_avatar": "boolean",
    "avatar_url": "string",
    "is_global": "boolean",
    "default_llm_id": "long",
    "created_at": "timestamp",
    "updated_at": "timestamp"
  }
  ```

## 5. Przepływ danych

1. Klient wysyła żądanie PUT do `/api/characters/{id}` wraz z danymi w formacie JSON.
2. Warstwa kontrolera (np. `CharacterController`) przyjmuje dane, waliduje je i mapuje do obiektu `UpdateCharacterCommand`.
3. Kontroler wywołuje metodę serwisową (np. `CharacterService.updateCharacter(id, command, currentUser)`) odpowiedzialną za logikę aktualizacji postaci.
4. Serwis:
   - Weryfikuje, czy postać o podanym ID istnieje.
   - Sprawdza, czy aktualnie zalogowany użytkownik jest właścicielem postaci (lub posiada odpowiednie uprawnienia do modyfikacji postaci globalnych).
   - Waliduje unikalność nazwy postaci (unikalność w obrębie użytkownika).
   - Aktualizuje odpowiednie pola w encji `Character` i ustawia datę `updated_at`.
   - Zapisuje zmiany do bazy danych przy użyciu repozytorium (np. `CharacterRepository`).
5. Zaktualizowana encja jest mapowana na DTO odpowiedzi i zwracana do klienta.

## 6. Względy bezpieczeństwa

- **Uwierzytelnianie i autoryzacja:** Endpoint powinien być zabezpieczony przy użyciu Spring Security. Tylko zalogowani użytkownicy mogą korzystać z tej funkcjonalności.
- **Weryfikacja właściciela:** Upewnij się, że użytkownik aktualizujący postać jest jej właścicielem lub posiada uprawnienia do modyfikacji postaci globalnych. W przeciwnym wypadku zwróć kod 403 Forbidden.
- **Walidacja danych:** Użyj walidacji danych (np. adnotacje `@Valid`, `@NotBlank`, `@Size`), aby upewnić się, że dane wejściowe są prawidłowe.

## 7. Obsługa błędów

- **400 Bad Request:** Błędne dane wejściowe, np. nieprawidłowy format lub naruszenie walidacji.
- **401 Unauthorized:** Brak autoryzacji, jeśli użytkownik nie jest zalogowany.
- **403 Forbidden:** Użytkownik próbuje zaktualizować postać, którą nie posiada lub nie ma do niej dostępu (np. globalna postać).
- **404 Not Found:** Postać o podanym identyfikatorze nie istnieje.
- **409 Conflict:** Nazwa postaci już istnieje (naruszenie unikalności w obrębie użytkownika).
- **500 Internal Server Error:** Błąd po stronie serwera, nieprzewidziany wyjątek.

## 8. Rozważenia dotyczące wydajności

- Zapytanie powinno być zoptymalizowane. Upewnij się, że kluczowe operacje mają odpowiednie indeksy (np. indeks na `user_id` i `name`).
- Aktualizacja postaci najprawdopodobniej nie będzie obciążać systemu, ale należy monitorować potencjalne problemy przy bardzo dużej liczbie operacji.
- Upewnij się, że operacje na bazie danych są odpowiednio zarządzane w kontekście transakcyjnym, aby zapewnić spójność danych.

## 9. Etapy wdrożenia

1. **Definicja DTO i Command Model**
   - Upewnij się, że `UpdateCharacterCommand` jest poprawnie zdefiniowany (patrz: `/be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/model/character/request/UpdateCharacterCommand.java`).
   - Utwórz lub zaktualizuj DTO odpowiedzi, aby odpowiadał wymaganiom specyfikacji.

2. **Implementacja Kontrolera**
   - Dodaj nową metodę w `CharacterController`, obsługującą żądanie PUT na ścieżce `/api/characters/{id}`.
   - Mapuj dane wejściowe do obiektu `UpdateCharacterCommand` i przekazuj je do warstwy serwisowej.

3. **Implementacja Logiki w Warstwie Serwisowej**
   - Utwórz lub zaktualizuj metodę w `CharacterService` do obsługi aktualizacji postaci.
   - Pobierz istniejącą postać przy użyciu `CharacterRepository`.
   - Zaimplementuj logikę walidacji właściciela i unikalności nazwy.
   - Aktualizuj pola postaci oraz datę `updated_at`.
   - Zapisz zmiany w bazie danych.

4. **Obsługa Wyjątków i Walidacja**
   - Dodaj walidację danych wejściowych z użyciem adnotacji Bean Validation.
   - Zaimplementuj globalny handler wyjątków (`@ControllerAdvice`), aby obsłużyć i mapować błędy na odpowiednie kody statusu HTTP.