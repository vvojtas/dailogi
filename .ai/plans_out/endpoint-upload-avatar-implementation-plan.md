# API Endpoint Implementation Plan: Upload Character Avatar

## 1. Przegląd punktu końcowego
Endpoint umożliwia użytkownikowi przesłanie lub zastąpienie awatara dla określonej postaci. Używając metody POST, użytkownik wysyła obraz (JPG/PNG, max 1MB, 256x256px) jako część multipart form-data. Endpoint aktualizuje pole `avatar` w tabeli `Character` i zwraca zaktualizowane informacje w formacie JSON.

## 2. Szczegóły żądania
- **Metoda HTTP**: POST
- **URL**: `/api/characters/{id}/avatar`
- **Parametry**:
  - **Path Variable**:
    - `id` (Long): Identyfikator postaci, której awatar jest aktualizowany.
  - **Body**:
    - Multipart form-data zawierający plik obrazu.
- **Walidacja**: 
  - Typ pliku: tylko JPG lub PNG.
  - Maksymalny rozmiar: 1MB.
  - Wymiary obrazu: dokładnie 256x256 pikseli.

## 3. Wykorzystywane typy
- **DTO**: `CharacterAvatarResponseDTO` zawierający pola:
  - `id`: Long
  - `has_avatar`: boolean
  - `avatar_url`: string
- **Command Model**: Opcjonalnie `UploadAvatarCommand` przechowujący przesłany plik i identyfikator postaci (jeśli potrzebne wewnętrznie).
- **Encja**: `Character` z kolumną `avatar` typu BYTEA.

## 4. Szczegóły odpowiedzi
- **Sukces**: HTTP 200 OK
  - Treść odpowiedzi: JSON odpowiadający strukturze `CharacterAvatarResponseDTO`.
- **Błędy**:
  - 400 Bad Request: Nieprawidłowy obraz (np. błędny format, rozmiar lub wymiary).
  - 401 Unauthorized: Użytkownik niezalogowany.
  - 403 Forbidden: Próba modyfikacji awatara postaci, która nie należy do zalogowanego użytkownika.
  - 404 Not Found: Nie znaleziono postaci o podanym `id`.

## 5. Przepływ danych
1. Klient wysyła żądanie POST z multipart form-data zawierającym obraz na `/api/characters/{id}/avatar`.
2. Filtry bezpieczeństwa/uwierzytelniania weryfikują, czy użytkownik jest zalogowany.
3. Kontroler:
   - Pobiera `id` z URL.
   - Odbiera plik obrazu z żądania.
4. Serwis:
   - Waliduje plik (typ, rozmiar, wymiary).
   - Pobiera encję `Character` z bazy danych (sprawdzenie, czy postać istnieje i należy do użytkownika).
   - Konwertuje obraz do formatu zgodnego z kolumną `avatar` (BYTEA).
   - Aktualizuje rekord `Character` i zapisuje zmiany.
   - Generuje lub aktualizuje `avatar_url` (opcjonalnie przez dodatkowy serwis zarządzania plikami).
5. Odpowiedź zwracana jest w formacie `CharacterAvatarResponseDTO`.

## 6. Względy bezpieczeństwa
- **Uwierzytelnianie**: Endpoint dostępny tylko dla zalogowanych użytkowników (np. przy użyciu JWT lub sesji).
- **Autoryzacja**: Weryfikacja, czy postać o zadanym `id` należy do bieżącego użytkownika. W przeciwnym razie zwracany jest kod 403 Forbidden.
- **Walidacja danych**: Szczegółowa walidacja przesłanego obrazu (typ, rozmiar, wymiary) w celu zapobieżenia atakom związanych z uploadem plików.
- **Bezpieczeństwo danych**: Przechowywanie obrazu w bazie danych jako BLOB (BYTEA) lub delegacja do bezpiecznego systemu przechowywania plików (np. chmura).
- **Logowanie**: Zapisywanie prób nieautoryzowanego dostępu lub błędów walidacji do systemu logowania.

## 7. Rozważania dotyczące wydajności
- **Optymalizacja walidacji obrazu**: Użycie efektywnych bibliotek do weryfikacji typu i rozmiaru obrazu.
- **Asynchroniczność**: Możliwość przetwarzania obrazów w tle, jeśli operacja jest zasobożerna.
- **Cache**: Ewentualne wykorzystanie cache dla avatar_url, aby zminimalizować obciążenie serwera.

## 8. Etapy wdrożenia
1. **Kontroler**:
   - Dodanie nowego endpointu w odpowiednim kontrolerze (np. `CharacterController` lub dedykowanym `AvatarController`).
   - Implementacja metody obsługującej żądania POST na `/api/characters/{id}/avatar`.
2. **Walidacja**:
   - Implementacja walidacji przesłanego pliku (typ, rozmiar, wymiary).
3. **Serwis biznesowy**:
   - Utworzenie lub rozbudowa istniejącego serwisu o logikę aktualizacji awatara dla postaci.
   - Pobieranie encji `Character` z repozytorium, walidacja właściciela, przetwarzanie obrazu i zapis danych.
4. **Repozytorium**:
   - Weryfikacja, czy istnieją odpowiednie metody w repozytorium dla encji `Character`.
5. **DTO i Command Model**:
   - Użycie `CharacterAvatarResponseDTO` do zwrócenia odpowiedzi.
   - Opcjonalnie stworzenie modelu `UploadAvatarCommand` do przenoszenia danych w warstwie serwisowej.
6. **Testy**:
   - Implementacja testów jednostkowych i integracyjnych dla kontrolera i serwisu.
   - Testowanie przypadków: sukces, nieprawidłowy plik, brak autoryzacji, niezgodność właściciela, brak postaci.
7. **Dokumentacja**:
   - Aktualizacja dokumentacji API oraz instrukcji wdrożenia.