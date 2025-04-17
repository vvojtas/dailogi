# API Endpoint Implementation Plan: Get Character

## 1. Przegląd punktu końcowego
Endpoint ten umożliwia pobranie danych pojedynczego postaci na podstawie jego unikalnego identyfikatora. Operacja realizowana jest metodą GET i zwraca dane w formacie `CharacterDTO`.

## 2. Szczegóły żądania
- **Metoda HTTP**: GET
- **Ścieżka URL**: `/api/characters/{id}`
- **Parametry**:
  - **Wymagane**:
    - `id` (Long): Identyfikator postaci, przekazywany jako parametr ścieżki.
- **Ciało żądania**: Brak

## 3. Wykorzystywane typy
- **DTO**: `CharacterDTO` (zdefiniowany w `be/dailogi-server/src/main/java/com/github/vvojtas/dailogi_server/model/character/response/CharacterDTO.java`)
- **Entity**: Encja odpowiadająca tabeli `Character` w bazie danych.
- **Repozytorium**: `CharacterRepository` rozszerzające `JpaRepository<Character, Long>`
- **Command Model (opcjonalnie)**: Nie jest wymagany, gdyż operacja dotyczy wyłącznie odczytu danych.

## 4. Szczegóły odpowiedzi
- **Sukces (200 OK)**: Zwracana jest struktura JSON odpowiadająca `CharacterDTO`:
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
- **Błędy**:
  - `401 Unauthorized`: Użytkownik nie jest zalogowany.
  - `403 Forbidden`: Użytkownik nie posiada praw dostępu do danego postaci (np. postać nie jest globalna, a użytkownik nie jest jej właścicielem).
  - `404 Not Found`: Postać o podanym `id` nie istnieje.
  - `500 Internal Server Error`: Nieoczekiwane błędy po stronie serwera.

## 5. Przepływ danych
1. Żądanie jest wysyłane przez klienta do endpointu z parametrem `id`.
2. Warstwa kontrolera (np. `CharacterController`) weryfikuje autentykację użytkownika.
3. Kontroler deleguje operację do warstwy serwisowej (np. `CharacterService`), która:
   - Pobiera bieżącego użytkownika przy pomocy `CurrentUserService.getUser()`.
   - Pobiera postać z bazy danych za pomocą `CharacterRepository.findById(id)`.
   - Weryfikuje, czy postać istnieje. W przypadku braku rekordu - zwraca `404 Not Found`.
   - Sprawdza uprawnienia: jeżeli postać nie jest globalna, użytkownik musi być jej właścicielem, w przeciwnym razie zwraca `403 Forbidden`.
   - Mapuje encję postaci do DTO (`CharacterDTO`).
4. Wynik jest zwracany do klienta przez kontroler.

## 6. Względy bezpieczeństwa
- Endpoint musi być zabezpieczony przy pomocy Spring Security.
- Uwierzytelnienie: Użytkownik musi być poprawnie zalogowany (w przeciwnym razie 401 Unauthorized).
- Autoryzacja: Dla postaci nie globalnych użytkownik musi być właścicielem, co przy braku dostępu generuje 403 Forbidden.
- Walidacja danych wejściowych: Sprawdzenie poprawności formatu identyfikatora `id`.

## 7. Obsługa błędów
- **401 Unauthorized**: Brak poprawnych danych uwierzytelniających.
- **403 Forbidden**: Użytkownik nie ma prawa dostępu do danego zasobu.
- **404 Not Found**: Brak postaci o podanym `id`.
- **500 Internal Server Error**: Nieoczekiwane błędy; błędy są logowane za pomocą SLF4J oraz obsługiwane przez centralny mechanizm (np. `@ControllerAdvice`).

## 8. Rozważania dotyczące wydajności
- Optymalizacja zapytań w warstwie repozytorium (możliwe użycie fetch join, gdy wymagane jest pobranie dodatkowych zależności).
- Opcjonalne cache'owanie wyników, jeśli wystąpi duże obciążenie zapytań odczytu.
- Monitorowanie wydajności w środowisku produkcyjnym.

## 9. Etapy wdrożenia
1. **Implementacja kontrolera**:
   - Utworzenie metody w `CharacterController` z adnotacją `@GetMapping("/api/characters/{id}")`.
2. **Rozbudowa warstwy serwisowej**:
   - Dodanie metody `getCharacterById(Long id)` w `CharacterService`, która pobiera bieżącego użytkownika przy pomocy `CurrentUserService.getUser()` i weryfikuje uprawnienia dostępu do postaci.
3. **Modyfikacja repozytorium**:
   - Zapewnienie, że `CharacterRepository` posiada metodę `findById`.
4. **Mapowanie encji do DTO**:
   - Implementacja logiki mapowania encji `Character` na `CharacterDTO`.
5. **Weryfikacja uprawnień**:
   - Dodanie logiki sprawdzającej, czy użytkownik ma prawo dostępu do postaci (jeżeli postać nie jest globalna).
6. **Obsługa błędów i walidacja**:
   - Dodanie walidacji danych wejściowych oraz implementacja centralnej obsługi wyjątków przez `@ControllerAdvice`.