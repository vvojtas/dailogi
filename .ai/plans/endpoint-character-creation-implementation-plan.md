# API Endpoint Implementation Plan: Create Character

## 1. Przegląd punktu końcowego
Endpoint umożliwia tworzenie nowej postaci. Użytkownik wysyła dane dotyczące postaci, które są walidowane oraz zapisywane w bazie danych. Operacja jest wykonywana tylko przez uwierzytelnionych użytkowników i uwzględnia unikalność nazwy postaci w obrębie użytkownika.

## 2. Szczegóły żądania
- Metoda HTTP: POST
- Struktura URL: `/api/characters`
- Parametry:
  - Wymagane:
    - `name` (String)
    - `short_description` (String)
    - `description` (String)
  - Opcjonalne:
    - `default_llm_id` (Long)
- Request Body:
  ```json
  {
    "name": "string",
    "short_description": "string",
    "description": "string",
    "default_llm_id": "long (optional)"
  }
  ```

## 3. Wykorzystywane typy
- `CreateCharacterCommand` (record) - zawiera pola: `name`, `short_description`, `description`, `default_llm_id`
- Potencjalnie: DTO do mapowania odpowiedzi, np. `CharacterResponse`, który odwzorowuje encję postaci.

## 4. Szczegóły odpowiedzi
- Sukces:
  - Kod: 201 Created
  - Response Body:
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
- Błędy:
  - 400 Bad Request: Błędy walidacji danych wejściowych.
  - 401 Unauthorized: Brak autoryzacji.
  - 409 Conflict: Nazwa postaci już istnieje dla danego użytkownika.
  - 422 Unprocessable Entity: Limit postaci został osiągnięty.

## 5. Przepływ danych
1. Żądanie trafia do endpointu `/api/characters` poprzez metodę POST.
2. Kontroler odbiera dane jako `CreateCharacterCommand` i przekazuje je do warstwy serwisowej.
3. Warstwa serwisowa:
   - Autoryzuje użytkownika na podstawie Spring Security.
   - Waliduje dane wejściowe oraz sprawdza, czy żadne wymagane pole nie jest puste.
   - Weryfikuje, czy użytkownik nie przekroczył limitu postaci.
   - Mapuje `CreateCharacterCommand` do encji `Character` i zapisuje rekord w bazie danych.
   - Wyłapuje błąd bazy sprawdzający unikalność pola `name` dla bieżącego użytkownika.
   - Wyłapuje błąd bazy weryfikujący istnienie LLM dla `default_llm_id`
4. Repozytorium zapisuje nową postać, automatycznie ustawiając pola takie jak `created_at` i `updated_at`.
5. Warstwa serwisowa mapuje encję na DTO odpowiedzi, które jest zwracane przez kontroler jako odpowiedź HTTP 201 Created.

## 6. Względy bezpieczeństwa
- Endpoint musi być zabezpieczony poprzez Spring Security oraz mechanizmy uwierzytelniania.
- Autoryzacja: operacja jest dozwolona tylko dla uwierzytelnionych użytkowników.
- Walidacja danych wejściowych w celu eliminacji ataków SQL Injection lub XSS.
- Regularne logowanie operacji oraz błędów przy użyciu SLF4J.

## 7. Obsługa błędów
- 400 Bad Request: Błąd walidacji danych (np. brak wymaganych pól, niespełnienie warunków walidacyjnych).
- 401 Unauthorized: Użytkownik nie jest uwierzytelniony.
- 409 Conflict: Wykrycie konfliktu, gdy nazwa postaci już istnieje dla użytkownika.
- 422 Unprocessable Entity: Limit postaci został osiągnięty.
- Użycie `@ControllerAdvice` do centralnej obsługi błędów oraz zwracania spójnych komunikatów błędów.

## 8. Rozważania dotyczące wydajności
- Optymalizacja zapytań: Sprawdzenie unikalności nazwy wykorzystujące indeksy na kolumnach `user_id` i `name`.
- Operacja tworzenia postaci jako pojedyncza transakcja przy użyciu Spring Data JPA.
- Minimalizacja zapytań do bazy poprzez odpowiednie zarządzanie repozytoriami.

## 9. Etapy wdrożenia
1. Utworzenie nowego endpointu w kontrolerze (np. `CharacterController`) z mapowaniem POST `/api/characters`.
2. Implementacja metody `createCharacter` w warstwie serwisowej (`CharacterService`), uwzględniającej:
   - Autoryzację użytkownika.
   - Walidację danych wejściowych (sprawdzenie pustych pól, limit postaci).
3. Wstrzyknięcie repozytoriów (`CharacterRepository`) do serwisu.
4. Mapowanie `CreateCharacterCommand` do encji `Character` i zapis do bazy danych za pomocą repozytorium.
5. Konwersja encji na DTO odpowiedzi i zwrócenie wyniku z kodem HTTP 201 Created.
6. Dodanie obsługi błędów poprzez `@ControllerAdvice` oraz centralne logowanie błędów z użyciem SLF4J.