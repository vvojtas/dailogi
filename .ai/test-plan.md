# Plan Testów dla Projektu d-AI-logi

## 1. Wprowadzenie i cel testów
Celem testów jest zapewnienie wysokiej jakości aplikacji d-AI-logi poprzez kompleksowe pokrycie kluczowych funkcjonalności na poziomie frontendu oraz backendu. Testy mają na celu weryfikację poprawności implementacji, bezpieczeństwa, wydajności i użyteczności aplikacji, zgodnie z dokumentem wymagań produktowych.

## 2. Zakres testów
- **Frontend:** 
  - Walidacja formularzy (rejestracja, logowanie, tworzenie/edycja postaci).
  - Interaktywność komponentów React oraz strony generowane przez Astro.
  - Responsywność UI i poprawność wyświetlania komponentów Shadcn/ui.
  - Przepływy użytkownika, m.in. tworzenie sceny dialogowej oraz obserwacja generowanego dialogu.

- **Backend:**
  - Testy jednostkowe serwisów i kontrolerów Spring.
  - Testy integracyjne REST API (m.in. rejestracja, logowanie, zarządzanie postaciami, generowanie dialogu).
  - Testy bezpieczeństwa (autoryzacja, uwierzytelnianie oraz szyfrowanie kluczy API).
  - Obsługa błędów i walidacja danych wejściowych.

- **Integracja i End-to-End:**
  - Testy przepływu użytkownika od rejestracji, poprzez konfigurację sceny, aż do generowania dialogu.
  - Testowanie integracji z zewnętrznym API OpenRouter (symulacja poprawnych i niepoprawnych scenariuszy).

## 3. Strategie testowania

### Frontend
- **Testy jednostkowe:** Użycie narzędzi takich jak Vitest, Jest oraz React Testing Library do testowania poszczególnych komponentów. Vitest jest preferowany ze względu na szybkość i lepszą integrację z Astro.
- **Testy integracyjne:** Weryfikacja współdziałania komponentów na poziomie stron Astro.
- **Testy E2E:** Automatyzacja scenariuszy użytkownika przy użyciu narzędzi takich jak Playwright lub Cypress, symulacja kluczowych przepływów (rejestracja, logowanie, tworzenie dialogu). Playwright jest rekomendowany jako nowocześniejsza alternatywa.

### Backend
- **Testy jednostkowe:** Wykorzystanie JUnit 5 i Mockito do testowania logiki biznesowej oraz kontrolerów Spring.
- **Testy integracyjne:** Testowanie komunikacji między warstwami aplikacji przy użyciu Spring Boot Test (np. MockMvc, testy z bazą H2 lub symulacją PostgreSQL).
- **Testy bezpieczeństwa:** Weryfikacja mechanizmów uwierzytelniania, autoryzacji oraz szyfrowania kluczy API.

### End-to-End (E2E) i Systemowe
- Automatyzacja pełnych scenariuszy użytkownika, łączących interakcje frontendowe z backendowymi endpointami.
- Symulacja scenariuszy awaryjnych, takich jak błędy API, nieprawidłowe dane wejściowe oraz przerwanie generowania dialogu.

## 4. Typy testów do przeprowadzenia
- **Testy jednostkowe:** Sprawdzające izolowane funkcje komponentów frontendu oraz metod backendowych.
- **Testy integracyjne:** Weryfikujące współdziałanie różnych modułów (np. kontroler-serwis, komunikacja z bazą danych).
- **Testy end-to-end (E2E):** Symulacja pełnych przepływów użytkownika.
- **Testy regresyjne:** Powtarzane testy całej aplikacji po każdej iteracji, w celu wykrycia nowych błędów.
- **Testy bezpieczeństwa:** Sprawdzenie autoryzacji, uwierzytelniania, szyfrowania oraz odporności na ataki typu injection.
- **Testy obciążeniowe (opcjonalnie):** Weryfikacja wydajności generowania dialogu oraz obsługi dużej liczby jednoczesnych użytkowników.

## 5. Środowiska testowe
- **Lokalne środowisko developerskie:** Do szybkich iteracji testów jednostkowych i integracyjnych.
- **Środowisko testowe/Staging:** Symulujące warunki produkcyjne, z pełną konfiguracją bazy danych (np. klon PostgreSQL) oraz integracji z zewnętrznymi serwisami.
- **Środowisko CI/CD:** Automatyczne testowanie przy każdej zmianie kodu (integracja z GitHub Actions). Testy powinny być uruchamiane nie tylko przy każdej zmianie, ale również w ramach pipeline'ów dla pull requestów przed umożliwieniem merge'u do głównej gałęzi.

## 6. Harmonogram testów (ogólny zarys)
1. **Faza planowania:**
   - Zdefiniowanie zakresu testów i przygotowanie środowisk.
2. **Testy jednostkowe:**
   - Opracowanie i uruchomienie testów jednostkowych dla komponentów frontendu i metod backendowych.
3. **Testy integracyjne:**
   - Przeprowadzenie testów integracyjnych w celu weryfikacji komunikacji między modułami.
4. **Testy E2E:**
   - Implementacja automatycznych scenariuszy użytkownika, symulacja pełnych przepływów.
5. **Testy regresyjne:**
   - Powtarzanie testów po każdej iteracji wdrożenia zmian.
6. **Testy bezpieczeństwa i wydajności:**
   - Testy pod kątem bezpieczeństwa oraz obciążeniowe (jeśli wymagane).

## 7. Kryteria akceptacji testów
- Wszystkie krytyczne funkcjonalności muszą przejść testy bez błędów.
- Walidacja danych wejściowych oraz obsługa błędów zgodnie z wymaganiami produktowymi.
- UI musi być responsywny i zgodny z projektem (Astro, React, Tailwind, Shadcn/ui).
- End-to-end testy muszą potwierdzić poprawny przepływ użytkownika, łącznie z generowaniem dialogu do 50 tur oraz zapisywaniem historii.
- Mechanizmy bezpieczeństwa (uwierzytelnianie, autoryzacja, szyfrowanie) muszą działać zgodnie z założeniami.

## 8. Proces raportowania błędów
- Błędy będą zgłaszane w systemie zarządzania zgłoszeniami (np. Jira).
- Każde zgłoszenie powinno zawierać:
  - Opis błędu oraz kroki do jego reprodukcji.
  - Oczekiwany rezultat versus otrzymany rezultat.
  - Zrzuty ekranu, logi systemowe oraz, w miarę możliwości, linki do commitów.
- Priorytetyzacja zgłoszeń według wpływu na krytyczne funkcjonalności oraz bezpieczeństwo.

## 9. Zasoby i narzędzia potrzebne do testowania
- **Frontend:**
  - Vitest jako główne narzędzie do testów (szybsze i lepiej zintegrowane z Astro)
  - Jest, React Testing Library jako narzędzia pomocnicze
  - Playwright (preferowany) lub Cypress do testów E2E
  - ESLint, Prettier

- **Backend:**
  - JUnit 5, Mockito, Spring Boot Test
  - Postman/Insomnia do testowania API
  - JaCoCo (narzędzie do analizy pokrycia testów)

- **Bezpieczeństwo i jakość kodu:**
  - SonarQube do analizy statycznej kodu i monitorowania jakości w czasie rzeczywistym

- **Inne:**
  - System CI/CD (np. GitHub Actions) do automatyzacji testów z naciskiem na testowanie wszystkich PR-ów.
  - Narzędzie do zarządzania błędami (Jira, Trello lub inny).
  - Środowiska Docker do symulacji produkcyjnych konfiguracji (jeśli wymagane).

## 10. Monitorowanie jakości kodu
- Implementacja SonarQube do ciągłej analizy jakości kodu
- Monitorowanie metryk jakości kodu w czasie rzeczywistym:
  - Pokrycie testami
  - Dług techniczny
  - Duplikacje kodu
  - Złożoność cyklomatyczna
  - Potencjalne problemy bezpieczeństwa
- Definiowanie progów jakości, które muszą być spełnione przed zaakceptowaniem pull requestów
