# API Endpoint Implementation Plan: Get LLMs

## 1. Przegląd punktu końcowego
Ten endpoint umożliwia pobranie dostępnych modeli językowych (LLM) przechowywanych w tabeli bazy danych `LLM`. Funkcjonalność obejmuje zwracanie listy modeli w postaci obiektów DTO odpowiadających rekordom z bazy.

## 2. Szczegóły żądania
- **Metoda HTTP:** GET
- **Struktura URL:** /api/llms
- **Parametry:**
  - Wymagane: Brak
  - Opcjonalne: Brak
- **Request Body:** Brak

## 3. Wykorzystywane typy
- **DTO:** `LLMDTO` (definiowany jako record) zawierający pola:
  - `id` (Long)
  - `name` (String)
  - `openrouterIdentifier` (String)
- **Encja:** Model odpowiadający tabeli `LLM` w bazie danych
- **Repository:** Interfejs rozszerzający `JpaRepository<LLM, Long>`
- **Service:** Warstwa serwisowa (np. `LLMService`) odpowiedzialna za pobieranie danych z repozytorium i mapowanie ich do DTO
- **Controller:** REST Controller (np. `LLMController`) obsługujący żądanie GET

## 4. Szczegóły odpowiedzi
- **Struktura odpowiedzi:** Tablica obiektów DTO, gdzie każdy obiekt zawiera:
  - `id`: liczba identyfikacyjna
  - `name`: nazwa modelu
  - `openrouter_identifier`: identyfikator zgodny z Openrouter
- **Kody statusu:**
  - 200 OK – przy pomyślnym pobraniu listy modeli
  - 500 Internal Server Error – dla nieoczekiwanych błędów serwera

## 5. Przepływ danych
1. Klient wysyła żądanie GET na `/api/llms`.
2. Authoryzacja w endpoincie jest pominięta
3. Żądanie trafia do kontrolera (`LLMController`), który deleguje wywołanie do warstwy serwisowej (`LLMService`).
4. `LLMService`, oznaczony jako `@Transactional(readOnly = true)`, wywołuje odpowiednią metodę w repozytorium (`LLMRepository`) aby pobrać wszystkie rekordy z tabeli `LLM`.
5. Pobierane encje są mapowane do obiektów DTO (`LLMDTO`).
6. Lista DTO zwracana jest do kontrolera, który zwraca ją jako wynik odpowiedzi HTTP 200.

## 6. Względy bezpieczeństwa
- Endpoint nie wymaga autoryzacji


## 7. Obsługa błędów
- **500 Internal Server Error:** Zwracany w przypadku nieoczekiwanych błędów podczas przetwarzania żądania. Użycie globalnego mechanizmu obsługi błędów (np. `@ControllerAdvice`) do centralizacji obsługi wyjątków.
- Logowanie błędów przy pomocy SLF4J dla zapewnienia możliwości diagnostyki problemów.

## 8. Rozważenia dotyczące wydajności
- Użycie adnotacji `@Transactional(readOnly = true)` w warstwie serwisowej aby zoptymalizować operacje odczytu.
- Indeksowanie kolumn używanych przy wyszukiwaniu w tabeli `LLM` – chociaż w tym przypadku zapytanie jest proste, warto mieć to na uwadze przy skalowaniu.
- Korzystanie z repozytoriów Spring Data JPA zapewnia optymalizację zapytań przy korzystaniu z fetch join, jeżeli pojawi się potrzeba rozbudowy logiki.

## 9. Etapy wdrożenia
1. **Repozytorium:**
   - Utworzenie lub uzupełnienie interfejsu `LLMRepository` rozszerzającego `JpaRepository<LLM, Long>`.
2. **Serwis:**
   - Utworzenie serwisu `LLMService` z metodą `List<LLMDTO> getLLMs()`.
   - Oznaczenie metody adnotacją `@Transactional(readOnly = true)`.
   - Mapowanie encji LLM na obiekty DTO (np. przy pomocy Java Streams lub biblioteki mapującej).
3. **Kontroler:**
   - Utworzenie klasy `LLMController` z adnotacją `@RestController`.
   - Utworzenie endpointu GET `/api/llms` wywołującego serwis i zwracającego wynik w postaci JSON.
4. **Konfiguracja bezpieczeństwa:**
   - Zapewnienie, że endpoint jest dostępny bez autoryzacji
5. **Dokumentacja:**
   - Uaktualnienie dokumentacji API oraz ewentualne uzupełnienie narzędzi do automatycznego generowania dokumentacji (np. Swagger/OpenAPI).