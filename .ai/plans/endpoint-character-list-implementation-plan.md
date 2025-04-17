# API Endpoint Implementation Plan: Get Characters

## 1. Przegląd punktu końcowego
Endpoint ma za zadanie zwrócić listę postaci, zarówno globalnych jak i przypisanych do obecnie zalogowanego użytkownika. Umożliwia to przeglądanie postaci w systemie, z możliwością stronicowania wyników oraz filtrowania wg. globalnych postaci.

## 2. Szczegóły żądania
- **Metoda HTTP**: GET
- **Ścieżka URL**: /api/characters
- **Parametry zapytania**:
  - `include_global` (opcjonalny, domyślnie true): flaga wskazująca, czy uwzględnić globalne postaci
  - `page` (opcjonalny, domyślnie 0): numer strony w paginacji
  - `size` (opcjonalny, domyślnie 20): liczba elementów na stronie
- **Request Body**: Brak, wszystkie informacje przekazywane są poprzez parametry query

## 3. Wykorzystywane typy
- `CharacterDTO`: DTO reprezentujący pojedynczą postać, zawiera pola takie jak id, name, short_description, description, has_avatar, avatar_url, is_global, default_llm_id, created_at oraz updated_at.
- `CharacterListDTO`: DTO dla paginowanej listy postaci, zawiera listę `CharacterDTO` oraz informacje paginacyjne: page, size, total_elements, total_pages.

## 4. Szczegóły odpowiedzi
- **Kod stanu**: 200 OK przy powodzeniu
- **Struktura odpowiedzi (JSON)**:
  ```json
  {
    "content": [
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
    ],
    "page": "integer",
    "size": "integer",
    "total_elements": "long",
    "total_pages": "integer"
  }
  ```
- **Kody błędów**:
  - 401 Unauthorized: brak odpowiedniej autoryzacji
  - 400 Bad Request: nieprawidłowe dane wejściowe
  - 500 Internal Server Error: błąd serwera

## 5. Przepływ danych
1. Żądanie przychodzi do kontrolera REST, który mapuje je na metodę obsługującą GET `/api/characters`.
2. Kontroler weryfikuje autentykację użytkownika (np. przy użyciu Spring Security).
3. Parametry zapytania (`include_global`, `page`, `size`) są walidowane i przekazywane do warstwy service.
4. Warstwa service korzysta z repozytorium (np. `CharacterRepository`) do wykonania zapytania na bazie danych, stosując:
   - Filtrowanie po `user_id` dla postaci należących do użytkownika
   - Warunek `is_global` jeśli parametr `include_global` jest true
5. Wyniki są mapowane do `CharacterDTO` i pakowane w obiekt `CharacterListDTO` wraz z danymi paginacyjnymi (page, size, total_elements, total_pages).
6. Odpowiedź jest zwracana w formacie JSON do klienta.

## 6. Względy bezpieczeństwa
- Uwierzytelnianie: Endpoint jest zabezpieczony mechanizmami Spring Security. Dostęp mają jedynie zalogowani użytkownicy.
- Autoryzacja: Weryfikacja, czy użytkownik ma dostęp do własnych zasobów.
- Walidacja danych wejściowych: Parametry zapytania są sprawdzane pod kątem prawidłowości (typy, zakres wartości) aby zapobiec potencjalnym atakom, np. SQL Injection.

## 7. Obsługa błędów
- 401 Unauthorized: Zwracany gdy użytkownik nie posiada ważnych poświadczeń.
- 400 Bad Request: Przy błędnych parametrach wejściowych (np. niepoprawne formaty liczbowe w `page` lub `size`).
- 500 Internal Server Error: Dla nieoczekiwanych błędów aplikacji lub bazy danych.
- Błędy te powinny być logowane przy użyciu SLF4J/Logback dla łatwiejszego debugowania i monitoringu.

## 8. Rozważania dotyczące wydajności
- Paginacja: Użycie paginacji ogranicza ilość danych przetwarzanych i przesyłanych w jednym zapytaniu.
- Optymalizacja zapytań: Zapewnienie, że zapytania do bazy danych korzystają z indeksów (np. na kolumnach `user_id` i `is_global`).
- Caching: Rozważenie cache'owania wyników przy dużym obciążeniu.

## 9. Etapy wdrożenia
1. Utworzenie kontrolera REST z mappingiem na `/api/characters`.
2. Implementacja weryfikacji autentykacji użytkownika za pomocą Spring Security.
3. Walidacja parametrów zapytania (include_global, page, size) w kontrolerze lub przy użyciu DTO walidacyjnych.
4. Implementacja warstwy service odpowiedzialnej za logikę biznesową:
   - Przetwarzanie i weryfikacja parametrów
   - Pobieranie danych z bazy poprzez repozytorium
   - Mapowanie wyników do DTO (CharacterDTO, CharacterListDTO)
5. Rozbudowa repozytorium (np. `CharacterRepository`) o metody wyszukiwania z uwzględnieniem filtrów (user_id, is_global).
6. Implementacja mechanizmów obsługi błędów i logowania (obsługa wyjątków, konfiguracja SLF4J/Logback).