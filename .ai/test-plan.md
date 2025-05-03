      
# Plan Testów dla Aplikacji d-AI-logi

**Wersja:** 1.0
**Data:** 2025-05-03
**Autor:** Gemini 2.5 PRO, Specjalista QA

## 1. Wprowadzenie i Cel Testów

### 1.1. Wprowadzenie
Niniejszy dokument opisuje plan testów dla aplikacji internetowej **d-AI-logi** w wersji MVP (Minimum Viable Product). Aplikacja umożliwia użytkownikom tworzenie i obserwowanie dialogów generowanych przez sztuczną inteligencję (AI) pomiędzy zdefiniowanymi postaciami. Wykorzystuje architekturę opartą o frontend (Astro + React) oraz backend (Java + Spring Boot), z integracją zewnętrznego API (OpenRouter) do obsługi modeli językowych (LLM).

### 1.2. Cel Testów
Głównym celem testów jest zapewnienie wysokiej jakości aplikacji d-AI-logi w wersji MVP poprzez:
*   Weryfikację zgodności implementacji z wymaganiami funkcjonalnymi (PRD).
*   Identyfikację i raportowanie defektów oprogramowania.
*   Ocenę stabilności, wydajności i bezpieczeństwa kluczowych komponentów aplikacji.
*   Zapewnienie spójnego i użytecznego doświadczenia użytkownika (UX).
*   Minimalizację ryzyka związanego z wdrożeniem aplikacji na środowisko produkcyjne.

## 2. Zakres Testów

### 2.1. Funkcjonalności Wchodzące w Zakres Testów (In Scope)
Testy obejmą wszystkie funkcjonalności zdefiniowane w dokumencie wymagań (PRD) dla wersji MVP, w tym:

*   **Zarządzanie Kontem Użytkownika (FR-001):** Rejestracja, Logowanie.
*   **Zarządzanie Postaciami AI (FR-002):** Tworzenie (z limitem, walidacją, opcjonalnym awatarem), Edycja, Usuwanie, Przeglądanie własnych postaci.
*   **Biblioteka Postaci Predefiniowanych (FR-003):** Dostęp i przeglądanie dla zalogowanych i niezalogowanych użytkowników.
*   **Tworzenie Sceny Dialogowej (FR-004):** Wybór 2-3 postaci (własne/predefiniowane), Definiowanie tematu/opisu sceny, Wybór LLM per postać (z listy przez OpenRouter).
*   **Generowanie i Obserwacja Dialogu (FR-005):** Inicjacja, Generowanie tura po turze (round-robin), Limit 50 tur, Wizualizacja czatu.
*   **Zarządzanie Historią Dialogów (FR-006, US-017 - US-020):** Zapisywanie (z limitem), Przeglądanie listy, Odczytywanie zapisanego dialogu, Usuwanie zapisanego dialogu.
*   **Zarządzanie Kluczem API OpenRouter (FR-007):** Wprowadzanie, Zapisywanie (szyfrowane), Aktualizacja/Usuwanie.
*   **Mechanizm Specjalnego Użytkownika (FR-008):** Użycie globalnego klucza API.
*   **Obsługa Błędów API (FR-009):** Wyświetlanie komunikatów, Obsługa błędów podczas generowania dialogu.
*   **Interfejs Użytkownika (FR-010):** Podstawowa nawigacja, Strona startowa.
*   **Dostęp Gościa (US-021 - US-023):** Ograniczenia akcji, Przeglądanie postaci predefiniowanych.
*   **Backend API:** Walidacja endpointów, autentykacja (JWT), autoryzacja.
*   **Interakcja Frontend-Backend:** Poprawność przesyłania danych, obsługa odpowiedzi API.
*   **Baza Danych:** Integralność danych, poprawność migracji (Flyway).

### 2.2. Funkcjonalności Poza Zakresem Testów (Out of Scope)
Następujące elementy nie będą testowane w ramach MVP:

*   Funkcjonalności wymienione w PRD jako "NIE wchodzące w zakres MVP".
*   Zaawansowane testy wydajnościowe i obciążeniowe (poza podstawowymi sprawdzeniami).
*   Formalny audyt dostępności (WCAG).
*   Testy penetracyjne (poza podstawowymi testami bezpieczeństwa).
*   Testowanie specyficznych, niestandardowych konfiguracji przeglądarek lub systemów operacyjnych.
*   Dogłębna analiza jakości odpowiedzi generowanych przez modele LLM (skupienie na procesie generowania, a nie merytorycznej poprawności odpowiedzi AI).
*   Testowanie kosztów generowania zapytań do OpenRouter (poza podstawową weryfikacją działania mechanizmu).

## 3. Strategie Testowania

### 3.1. Strategia Testowania Frontendu (Astro + React)
*   **Testy Jednostkowe (Unit Tests):**
    *   Narzędzia: Vitest, React Testing Library (RTL).
    *   Zakres: Poszczególne komponenty React (izolowane), hooki, funkcje pomocnicze (np. walidacja po stronie klienta, formatowanie danych). Testowanie logiki renderowania, obsługi zdarzeń, zarządzania stanem komponentu.
*   **Testy Integracyjne Komponentów (Component Integration Tests):**
    *   Narzędzia: React Testing Library (RTL).
    *   Zakres: Interakcje pomiędzy powiązanymi komponentami React (np. formularz i jego pola, lista i jej elementy). Weryfikacja przepływu danych i zdarzeń między komponentami.
*   **Testy End-to-End (E2E):**
    *   Narzędzia: Playwright lub Cypress.
    *   Zakres: Symulacja pełnych przepływów użytkownika w przeglądarce, obejmująca interakcję z UI, komunikację z backendem i weryfikację rezultatów. Kluczowe scenariusze: rejestracja, logowanie, CRUD postaci (z awatarem), tworzenie sceny, inicjacja i obserwacja dialogu, przeglądanie historii. Testowanie zarówno statycznych stron Astro, jak i interaktywnych wysp React.
*   **Testy Manualne:**
    *   Zakres: Testy eksploracyjne, testy użyteczności (UX), weryfikacja wizualna (zgodność z designem, responsywność - Tailwind), testy kompatybilności przeglądarek.

### 3.2. Strategia Testowania Backendu (Java + Spring Boot)
*   **Testy Jednostkowe (Unit Tests):**
    *   Narzędzia: JUnit 5, Mockito.
    *   Zakres: Poszczególne klasy serwisów (np. `CharacterService`, `AuthService`), kontrolerów (mockując zależności), repozytoriów (jeśli zawierają złożoną logikę zapytań), klas pomocniczych (np. `AvatarUtil`, `JwtTokenProvider`). Skupienie na logice biznesowej w izolacji.
*   **Testy Integracyjne (Integration Tests):**
    *   Narzędzia: Spring Boot Test (`@SpringBootTest`), H2 (dla szybkich testów) / Testcontainers (dla testów z PostgreSQL), MockMvc / RestAssured (dla testów API).
    *   Zakres: Weryfikacja interakcji pomiędzy warstwami aplikacji (Kontroler -> Serwis -> Repozytorium -> Baza Danych). Testowanie endpointów API (walidacja żądań, odpowiedzi, kody statusu), konfiguracji bezpieczeństwa (Spring Security - reguły dostępu, filtry JWT), obsługi wyjątków (`GlobalExceptionHandler`), migracji bazy danych (Flyway). Zewnętrzne zależności (np. OpenRouter API) będą mockowane.
*   **Testy API (API Contract Tests):**
    *   Narzędzia: Postman, Newman lub RestAssured.
    *   Zakres: Testowanie kontraktu REST API niezależnie od UI. Weryfikacja wszystkich endpointów, metod HTTP, struktur żądań i odpowiedzi, obsługi błędów, autentykacji i autoryzacji zgodnie ze specyfikacją OpenAPI (`docs/be-api.json`).

### 3.3. Strategia Testowania Integracji AI (OpenRouter)
*   **Mockowanie w Testach Jednostkowych/Integracyjnych:** Serwis OpenRouter będzie mockowany, aby symulować różne scenariusze odpowiedzi (sukces, błąd klucza, błąd serwera, przekroczenie limitu) bez rzeczywistego wywoływania API.
*   **Dedykowane Testy Integracyjne API:** Przeprowadzenie ograniczonej liczby testów na środowisku Staging, wykorzystując dedykowany, *testowy* klucz API OpenRouter z niskimi limitami. Celem jest weryfikacja poprawności komunikacji (format zapytań, autoryzacja) i obsługi rzeczywistych odpowiedzi API. Należy monitorować koszty tych testów.
*   **Testowanie Obsługi Błędów:** Skupienie na weryfikacji, czy aplikacja poprawnie interpretuje błędy zwracane przez OpenRouter i prezentuje je użytkownikowi (FR-009, US-016).

## 4. Typy Testów

W ramach projektu przeprowadzone zostaną następujące typy testów:

*   **Testy Funkcjonalne:** Weryfikacja, czy aplikacja działa zgodnie z wymaganiami PRD (obejmuje testy jednostkowe, integracyjne, API, E2E).
*   **Testy Jednostkowe:** Izolowane testowanie najmniejszych części kodu (funkcje, metody, komponenty).
*   **Testy Integracyjne:** Testowanie interakcji pomiędzy różnymi modułami/warstwami aplikacji (np. kontroler-serwis-repozytorium, komponenty FE).
*   **Testy API:** Bezpośrednie testowanie endpointów backendu w celu weryfikacji kontraktu API.
*   **Testy End-to-End (E2E):** Testowanie kompletnych przepływów użytkownika przez całą aplikację (UI -> Backend -> DB -> Zewnętrzne API).
*   **Testy Bezpieczeństwa:** Podstawowa weryfikacja mechanizmów autentykacji i autoryzacji, przechowywania kluczy API, obsługa sesji (JWT), potencjalne podatności (np. XSS w polach tekstowych). Skanowanie zależności.
*   **Testy Użyteczności (Manualne):** Ocena łatwości obsługi, intuicyjności interfejsu i ogólnego doświadczenia użytkownika.
*   **Testy Kompatybilności (Manualne/Automatyczne):** Sprawdzenie działania aplikacji na różnych, popularnych przeglądarkach internetowych (Chrome, Firefox, Safari, Edge - najnowsze wersje).
*   **Testy Regresji:** Ponowne wykonanie wybranych testów (automatycznych i manualnych) po wprowadzeniu zmian w kodzie lub naprawie błędów, aby upewnić się, że nowe zmiany nie zepsuły istniejących funkcjonalności.

## 5. Środowiska Testowe

*   **Środowisko Lokalne (Development):**
    *   Cel: Testowanie jednostkowe i integracyjne przez deweloperów podczas kodowania.
    *   Konfiguracja: Kod uruchamiany lokalnie, backend z bazą H2 lub lokalnym PostgreSQL, frontend łączący się z lokalnym backendem. Mockowane API OpenRouter.
*   **Środowisko CI (Continuous Integration - GitHub Actions):**
    *   Cel: Automatyczne uruchamianie testów jednostkowych, integracyjnych (z H2/Testcontainers) oraz E2E (opcjonalnie) po każdym pushu do repozytorium lub Pulla Request.
    *   Konfiguracja: Środowisko budowane dynamicznie w ramach pipeline'u CI.
*   **Środowisko Staging/Przedprodukcyjne:**
    *   Cel: Testy E2E, testy manualne, testy UAT (User Acceptance Testing), testy integracyjne z zewnętrznymi serwisami (ograniczone testy OpenRouter).
    *   Konfiguracja: Dedykowany serwer z konfiguracją zbliżoną do produkcyjnej. Własna instancja bazy danych PostgreSQL (z danymi testowymi). Połączenie z *testowym* kluczem API OpenRouter.
*   **Środowisko Produkcyjne:**
    *   Cel: Ograniczone testy dymne (smoke tests) po wdrożeniu nowej wersji. Bieżący monitoring działania aplikacji.
    *   Konfiguracja: Środowisko dostępne dla użytkowników końcowych.

## 6. Harmonogram Testów (Ogólny Zarys)

Testowanie będzie procesem ciągłym, zintegrowanym z cyklem rozwoju oprogramowania:

*   **Podczas Sprintu/Iteracji:**
    *   Deweloperzy piszą i uruchamiają testy jednostkowe i integracyjne równolegle z implementacją funkcjonalności.
    *   Testy automatyczne (jednostkowe, integracyjne) są uruchamiane w CI przy każdym pushu/PR.
    *   QA przeprowadza testy funkcjonalne (manualne/API) nowo zaimplementowanych historyjek użytkownika na środowisku lokalnym lub wczesnym Staging.
*   **Przed Zakończeniem Sprintu/Wydania MVP:**
    *   Wykonanie pełnego cyklu testów regresji (automatycznych i kluczowych manualnych).
    *   Przeprowadzenie testów E2E na środowisku Staging.
    *   Testy kompatybilności i użyteczności.
    *   UAT (jeśli dotyczy).
*   **Po Wdrożeniu na Produkcji:**
    *   Wykonanie testów dymnych (smoke tests) w celu weryfikacji kluczowych funkcjonalności.

## 7. Kryteria Akceptacji Testów

### 7.1. Kryteria Wejścia (Rozpoczęcia Testów Fazy/Cyklu)
*   Kod źródłowy dla testowanych funkcjonalności jest w repozytorium.
*   Build aplikacji jest możliwy i zakończony sukcesem.
*   Testy jednostkowe i podstawowe integracyjne (pokrywające kod) przechodzą pomyślnie w CI.
*   Środowisko testowe (Staging) jest dostępne i stabilne.
*   Dokumentacja (PRD, specyfikacja API) jest dostępna i aktualna.

### 7.2. Kryteria Wyjścia (Zakończenia Testów dla Wydania MVP)
*   Wszystkie zaplanowane przypadki testowe (manualne i automatyczne) zostały wykonane.
*   Wszystkie krytyczne (Critical) i wysokie (High) błędy zostały naprawione i zweryfikowane.
*   Brak znanych błędów blokujących kluczowe ścieżki użytkownika.
*   Odsetek błędów średnich (Medium) i niskich (Low) jest na akceptowalnym poziomie, zaakceptowanym przez Product Ownera.
*   Kluczowe testy E2E przechodzą pomyślnie na środowisku Staging.
*   Osiągnięto uzgodniony poziom pokrycia kodu testami automatycznymi (np. >70% dla testów jednostkowych i integracyjnych backnedu).
*   Wyniki testów zostały udokumentowane i zaakceptowane.

**Definicje Priorytetów Błędów:**
*   **Krytyczny (Critical):** Błąd blokujący działanie kluczowej funkcjonalności, brak obejścia, powoduje utratę danych lub awarię systemu.
*   **Wysoki (High):** Błąd znacznie utrudniający działanie kluczowej funkcjonalności, obejście jest trudne lub niewygodne.
*   **Średni (Medium):** Błąd utrudniający działanie mniej istotnej funkcjonalności lub istotnej z łatwym obejściem, problem UI/UX.
*   **Niski (Low):** Drobny błąd kosmetyczny, literówka, sugestia usprawnienia.

## 8. Proces Raportowania Błędów

*   **Narzędzie:** GitHub Issues w repozytorium projektu.
*   **Wymagane Informacje w Zgłoszeniu:**
    *   Tytuł: Krótki, zwięzły opis problemu.
    *   Środowisko: Gdzie zaobserwowano błąd (Lokalne, Staging, Produkcja).
    *   Wersja/Build: Jeśli dotyczy.
    *   Kroki do Reprodukcji: Dokładna, numerowana lista kroków.
    *   Wynik Oczekiwany: Co powinno się wydarzyć.
    *   Wynik Rzeczywisty: Co się wydarzyło.
    *   Priorytet/Waga (Severity): Krytyczny, Wysoki, Średni, Niski.
    *   Zrzuty Ekranu/Nagrania Wideo: Jeśli pomagają zilustrować problem.
    *   Logi: Fragmenty logów aplikacji (jeśli relevantne, szczególnie dla błędów backendu/API).
    *   Etykiety: np. `bug`, `frontend`, `backend`, `api`, `security`, `ui/ux`.
*   **Przepływ Pracy (Workflow) Błędu:**
    1.  `New/Open`: Zgłoszenie nowego błędu.
    2.  `Triage/Assigned`: Błąd jest analizowany, potwierdzany i przypisywany do dewelopera.
    3.  `In Progress`: Deweloper pracuje nad naprawą.
    4.  `Resolved/Ready for Verification`: Deweloper naprawił błąd i jest gotowy do weryfikacji przez QA.
    5.  `Verified`: QA potwierdza, że błąd został naprawiony na odpowiednim środowisku.
    6.  `Closed`: Błąd jest zamknięty.
    7.  `Reopened`: Jeśli weryfikacja nie powiodła się, błąd wraca do `In Progress`.

## 9. Zasoby i Narzędzia

### 9.1. Zasoby Ludzkie
*   Specjalista/Zespół QA
*   Deweloperzy Frontend
*   Deweloperzy Backend
*   Product Owner (do akceptacji kryteriów i priorytetyzacji błędów)

### 9.2. Narzędzia Testowe
*   **Frontend:**
    *   Vitest (Testy jednostkowe JS/TS)
    *   React Testing Library (Testy komponentów React)
    *   Playwright / Cypress (Testy E2E)
*   **Backend:**
    *   JUnit 5 (Framework testowy Java)
    *   Mockito (Mockowanie zależności Java)
    *   Spring Boot Test (Testy integracyjne Spring)
    *   H2 Database / Testcontainers (Testowanie bazy danych)
    *   MockMvc / RestAssured (Testowanie API z poziomu Javy)
*   **API:**
    *   Postman / Newman (Manualne i automatyczne testy API)
*   **CI/CD:**
    *   GitHub Actions
*   **Konteneryzacja:**
    *   Docker
*   **Zarządzanie Błędami:**
    *   GitHub Issues
*   **Analiza Kodu:**
    *   SonarQube (skonfigurowany w `pom.xml`)
*   **Przeglądarki:**
    *   Aktualne wersje Google Chrome, Mozilla Firefox, Safari, Microsoft Edge.
*   **Inne (Opcjonalnie):**
    *   Narzędzia do testów kompatybilności (np. BrowserStack, LambdaTest).
    *   Narzędzia do testów wydajnościowych (np. k6, JMeter).

    