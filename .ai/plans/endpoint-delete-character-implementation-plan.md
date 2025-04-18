# API Endpoint Implementation Plan: Delete Character

## 1. Przegląd punktu końcowego
Endpoint umożliwia usunięcie postaci (character) przez użytkownika. Działa pod adresem `DELETE /api/characters/{id}` i służy do usuwania postaci tylko wtedy, gdy użytkownik jest jej właścicielem oraz postać nie jest globalna ani powiązana z żadnymi dialogami.

## 2. Szczegóły żądania
- **Metoda HTTP**: DELETE
- **Struktura URL**: `/api/characters/{id}`
- **Parametry**:
  - Wymagane: `id` (typ: Long) – identyfikator postaci, która ma zostać usunięta.
  - Opcjonalne: brak
- **Request Body**: Brak

## 3. Szczegóły odpowiedzi
- **Pomyślna odpowiedź**:
  - Status: 200 OK
  - Body: `{ "message": "Character successfully deleted" }`
- **Błędy**:
  - 401 Unauthorized – użytkownik nie jest uwierzytelniony
  - 403 Forbidden – użytkownik nie jest właścicielem postaci
  - 404 Not Found – postać o podanym ID nie istnieje
  - 409 Conflict – postać jest używana w dialogach
  - 500 Internal Server Error – błąd po stronie serwera

## 4. Przepływ danych
1. **Odbiór żądania**: Kontroler przyjmuje żądanie DELETE na `/api/characters/{id}` i pobiera parametr `id` z URL.
2. **Autentykacja i autoryzacja**: Warstwa bezpieczeństwa weryfikuje tożsamość użytkownika. Jeśli użytkownik nie jest zalogowany, system zwraca 401 Unauthorized.
3. **Logika biznesowa w serwisie**:
   - Pobranie encji postaci z repozytorium na podstawie `id`.
   - Weryfikacja istnienia postaci – jeśli nie istnieje, zwrócenie 404 Not Found.
   - Sprawdzenie właściciela postaci (pole `user_id`). Jeśli użytkownik nie jest właścicielem, zwrócenie 403 Forbidden.
   - Jeśli baza zwróci błąd bo postać jest używana w dialogach, zwrócenie 409 Conflict.
   - Usunięcie postaci i zapisanie zmian w bazie danych.
4. **Odpowiedź**: Po pomyślnym usunięciu postaci, zwrócenie komunikatu z kodem 200 OK.

## 5. Względy bezpieczeństwa
- **Autentykacja**: Endpoint wymaga, by użytkownik był zalogowany.
- **Autoryzacja**: Upewnienie się, że tylko właściciel postaci może dokonać operacji.
- **Walidacja**: Sprawdzenie poprawności parametru `id` oraz warunki kontrolne związane z encją (np. istnienie i zależności).
- **Bezpieczeństwo danych**: Użycie transakcji podczas operacji usuwania, aby zapewnić spójność danych.

## 6. Obsługa błędów
- **401 Unauthorized**: Zwracane, gdy użytkownik nie jest zalogowany.
- **403 Forbidden**: Zwracane, gdy użytkownik nie jest właścicielem postaci lub gdy postać jest oznaczona jako globalna.
- **404 Not Found**: Zwracane, gdy nie można znaleźć postaci o podanym `id`.
- **409 Conflict**: Zwracane, gdy postać jest obecnie używana w dialogach, co uniemożliwia jej usunięcie.
- **500 Internal Server Error**: Uogólniony błąd serwera; szczegóły logowane przy użyciu SLF4J.

## 7. Wydajność
- **Optymalizacja zapytań**: Łączenie weryfikacji istnienia i własności postaci w jedno zapytanie do bazy danych, aby zminimalizować liczbę odwołań.
- **Transakcje**: Użycie krótkich transakcji, aby zapobiec blokadom i utrzymać spójność danych.
- **Indeksy**: Upewnienie się, że kolumny używane w zapytaniach (np. `id`, `user_id`) są indeksowane.

## 8. Kroki implementacji
1. **REST Controller**: Dodanie metody w kontrolerze obsługującą DELETE `/api/characters/{id}`.
2. **Walidacja wejścia**: Pobranie i walidacja parametru `id`.
3. **Service Layer**: Implementacja metody serwisowej, która:
   - Pobiera postać z repozytorium,
   - Weryfikuje istnienie postaci,
   - Sprawdza, czy użytkownik jest właścicielem,
   - Sprawdza referencje w dialogach,
   - Usuwa postać.
4. **Repozytorium**: Upewnienie się, że repozytorium posiada metodę do pobierania postaci
5. **Obsługa wyjątków**: Wdrożenie globalnego mechanizmu obsługi błędów (@ControllerAdvice) w celu mapowania wyjątków na odpowiednie kody statusu.
6. **Logowanie**: Wprowadzenie logowania przy użyciu SLF4J dla wszystkich błędów i operacji krytycznych.