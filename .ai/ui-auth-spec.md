# Specyfikacja Techniczna: Moduł Rejestracji i Logowania (UI + API Gateway)

Niniejszy dokument opisuje architekturę interfejsu użytkownika (UI) oraz logikę warstwy pośredniczącej (API Gateway w Astro) niezbędną do implementacji funkcjonalności rejestracji i logowania użytkowników w aplikacji d-AI-logi. Opiera się ona na wymaganiach z `@prd.md` (US-001, US-002), specyfikacji backendu `@be-auth-spec.md`, istniejącym planie UI `@ui-plan.md` oraz stosie technologicznym z `@tech-stack.md`.

## 1. Architektura Interfejsu Użytkownika (Frontend - Astro + React)

Zmiany w UI koncentrują się na dodaniu nowych widoków publicznych dla logowania i rejestracji, adaptacji istniejących layoutów i komponentów do obsługi stanu uwierzytelnienia oraz implementacji formularzy interaktywnych.

### 1.1. Nowe Strony (Astro Pages)

*   **`/login.astro` (`src/pages/login.astro`):**
    *   **Cel:** Strona logowania użytkownika.
    *   **Layout:** Wykorzystuje `Layout.astro` (lub podobny layout dla stron publicznych, bez nawigacji aplikacji).
    *   **Zawartość:** Nagłówek ("Logowanie"), osadzony komponent React `LoginForm.tsx`, link do strony rejestracji (`/register`).
    *   **Logika:** Strona jest statyczna, główna logika znajduje się w komponencie React. W przypadku, gdy użytkownik jest już zalogowany (sprawdzenie w `Astro.locals`), strona powinna automatycznie przekierować do `/dashboard`.

*   **`/register.astro` (`src/pages/register.astro`):**
    *   **Cel:** Strona rejestracji nowego użytkownika.
    *   **Layout:** Wykorzystuje `Layout.astro`.
    *   **Zawartość:** Nagłówek ("Rejestracja"), osadzony komponent React `RegisterForm.tsx`, link do strony logowania (`/login`).
    *   **Logika:** Strona statyczna. Podobnie jak `/login`, jeśli użytkownik jest już zalogowany, następuje przekierowanie do `/dashboard`.

### 1.2. Modyfikacje Layoutów i Stron Istniejących

*   **`Layout.astro` (`src/layouts/Layout.astro`):**
    *   **Zmiany:** Główny layout aplikacji musi dynamicznie renderować elementy nawigacyjne w zależności od stanu uwierzytelnienia użytkownika przechowywanego w klienckim store (np. Zustand). Stan ten jest zarządzany po stronie klienta (np. aktualizowany po udanym logowaniu/wylogowaniu przez komponenty React).
        *   **Dla niezalogowanych:** W nagłówku/panelu bocznym wyświetlane są linki/przyciski "Zaloguj się" (`/login`) i "Zarejestruj się" (`/register`). Brak dostępu do nawigacji chronionej.
        *   **Dla zalogowanych:** Wyświetlana jest pełna nawigacja aplikacji (Postaci, Historia Scen, Nowa Scena, Profil), nazwa zalogowanego użytkownika (pobrana ze stanu Zustand, np. `store.user.name`) oraz przycisk/link "Wyloguj".
    *   **Komponent Wylogowania:** Przycisk "Wyloguj" powinien być komponentem (np. `LogoutButton.tsx`), który po kliknięciu wywołuje dedykowany endpoint API (`POST /api/auth/logout`) w celu unieważnienia sesji po stronie serwera, a następnie czyści stan globalny (Zustand) i przekierowuje użytkownika na stronę główną (`/`).

*   **Strony Chronione (np. `/dashboard.astro`, `/characters/**`, `/scenes/**`, `/profile.astro`):**
    *   Nie wymagają bezpośrednich zmian w logice renderowania związanej z auth (middleware zajmie się przekierowaniem). Ewentualne dane użytkownika potrzebne na tych stronach (np. ID do pobrania zasobów) muszą być pobierane przez uwierzytelnione wywołania API (które użyją ciasteczka sesji) lub pochodzić ze stanu Zustand.

*   **Strona Główna (`/index.astro`):**
    *   Logika tej strony powinna opierać się na stanie klienckim (Zustand) do ewentualnego personalizowania powitania lub przekierowania na `/dashboard`, a nie na `Astro.locals.user`.

### 1.3. Nowe Komponenty Interaktywne (React)

*   **`LoginForm.tsx` (`src/components/auth/LoginForm.tsx`):**
    *   **Cel:** Formularz logowania.
    *   **Biblioteki:** `React Hook Form` (do zarządzania stanem i walidacją), `Zod` (do schematu walidacji).
    *   **Pola:**
        *   `Input` dla nazwy użytkownika (`name`).
        *   `Input type="password"` dla hasła (`password`).
        *   `Button type="submit"` ("Zaloguj się").
    *   **Walidacja (Client-side):**
        *   Nazwa użytkownika: Wymagana, minimalna/maksymalna długość (zgodna z backendem).
        *   Hasło: Wymagane, minimalna długość (zgodna z backendem).
        *   Komunikaty o błędach walidacji wyświetlane przy polach.
    *   **Logika:**
        *   Po submicji formularza i pomyślnej walidacji client-side, wysyła żądanie `POST /api/auth/login` z danymi (`name`, `password`).
        *   Obsługuje odpowiedź API:
            *   **Sukces (200 OK):**
                *   Opcjonalnie odbiera `UserDto` z odpowiedzi API.
                *   Aktualizuje stan globalny Zustand danymi użytkownika (`store.setUser(userDto)`).
                *   Przekierowuje użytkownika na `/dashboard` (np. używając `window.location.href` lub `navigate` z `astro:transitions` jeśli używane).
            *   **Błąd (400 Bad Request - np. nieprawidłowe dane logowania):** Wyświetla komunikat błędu globalny dla formularza lub używa `Toast` (np. "Nieprawidłowa nazwa użytkownika lub hasło.").
            *   **Inne błędy (5xx):** Wyświetla ogólny komunikat błędu (`Toast`).
        *   Wyświetla stan ładowania na przycisku podczas wysyłania żądania.

*   **`RegisterForm.tsx` (`src/components/auth/RegisterForm.tsx`):**
    *   **Cel:** Formularz rejestracji.
    *   **Biblioteki:** `React Hook Form`, `Zod`.
    *   **Pola:**
        *   `Input` dla nazwy użytkownika (`name`).
        *   `Input type="password"` dla hasła (`password`).
        *   `Input type="password"` dla potwierdzenia hasła (`passwordConfirmation`).
        *   `Button type="submit"` ("Zarejestruj się").
    *   **Walidacja (Client-side):**
        *   Nazwa użytkownika: Wymagana, minimalna/maksymalna długość, ewentualnie dozwolone znaki.
        *   Hasło: Wymagane, minimalna długość, może zawierać wymagania co do złożoności (np. cyfra, duża litera - jeśli zdefiniowane w backendzie).
        *   Potwierdzenie hasła: Musi być zgodne z hasłem.
        *   Komunikaty o błędach walidacji przy polach.
    *   **Logika:**
        *   Po submicji i walidacji client-side, wysyła żądanie `POST /api/auth/register` z danymi (`name`, `password`, `passwordConfirmation`).
        *   Obsługuje odpowiedź API:
            *   **Sukces (200 OK / 201 Created):** Wyświetla komunikat sukcesu (`Toast` - "Rejestracja pomyślna. Możesz się teraz zalogować.") i opcjonalnie przekierowuje na `/login`.
            *   **Błąd (400 Bad Request - np. błędy walidacji backendowej):** Wyświetla komunikaty błędów zwrócone przez API przy odpowiednich polach lub jako błąd globalny.
            *   **Błąd (409 Conflict - nazwa użytkownika zajęta):** Wyświetla komunikat błędu przy polu nazwy użytkownika ("Nazwa użytkownika jest już zajęta.").
            *   **Inne błędy (5xx):** Wyświetla ogólny komunikat błędu (`Toast`).
        *   Wyświetla stan ładowania na przycisku.

### 1.4. Stan Globalny (Zustand)

*   Store Zustand (`src/lib/store.ts` lub podobny) jest kluczowy do przechowywania informacji o zalogowanym użytkowniku (`user: UserDto | null`, `isLoggedIn: boolean`) po stronie klienta. Służy do szybkiego dostępu przez komponenty UI i dynamicznego renderowania interfejsu.
*   Stan ten powinien być aktualizowany przez komponenty React po udanych operacjach logowania (ustawienie danych użytkownika) i wylogowania (wyczyszczenie danych). Nie jest inicjalizowany na podstawie `Astro.locals.user` przez layout, ponieważ middleware (Opcja 2) nie dostarcza tych danych.

### 1.5. Obsługa Scenariuszy

*   **Logowanie:** Użytkownik wchodzi na `/login`, wypełnia formularz, klika "Zaloguj się". Formularz (`LoginForm.tsx`) wysyła dane do `/api/auth/login`. API (Astro) woła backend Spring. Po sukcesie, API (Astro) ustawia ciasteczko sesji (HTTP-only) i zwraca `200 OK` (opcjonalnie z `UserDto`). `LoginForm.tsx` odbiera sukces, aktualizuje store Zustand danymi użytkownika i przekierowuje na `/dashboard`. Middleware przy następnym żądaniu do *strony* chronionej tylko sprawdzi obecność ciasteczka, ale nie ustawi `Astro.locals.user`.
*   **Rejestracja:** Użytkownik wchodzi na `/register`, wypełnia formularz, klika "Zarejestruj się". Formularz wysyła dane do `/api/auth/register`. API (Astro) woła backend Spring. Po sukcesie, formularz wyświetla komunikat i przekierowuje na `/login`.
*   **Dostęp do strony chronionej (niezalogowany):** Użytkownik próbuje wejść np. na `/dashboard`. Middleware Astro przechwytuje żądanie, stwierdza brak ważnej sesji (ciasteczka), przekierowuje użytkownika na `/login`.
*   **Wylogowanie:** Użytkownik klika "Wyloguj". Komponent wysyła `POST /api/auth/logout`. API (Astro) woła backend (opcjonalne, jeśli jest blacklistowanie) i usuwa ciasteczko sesji. Komponent czyści stan Zustand i przekierowuje na `/`.

## 2. Logika Warstwy Pośredniczącej (Backend - Astro API Routes & Middleware)

Warstwa Astro pełni rolę API Gateway i zarządzania sesją użytkownika w kontekście aplikacji frontendowej, przekazując odpowiedzialność za weryfikację tokenów JWT do backendu Spring.

### 2.1. Middleware (`src/middleware/index.ts`)

*   **Cel:** Uproszczona ochrona tras stron Astro (bazująca tylko na obecności ciasteczka).
*   **Logika (Uproszczona - Opcja 2):**
    1.  Uruchamiany dla każdego żądania.
    2.  Sprawdza, czy ścieżka żądania dotyczy **strony chronionej** (np. `/dashboard`, `/profile`, `/scenes/**`, `/characters/new`, `/characters/[id]/edit`). Trasy publiczne (`/`, `/login`, `/register`) oraz endpointy API (`/api/**`) są ignorowane (API mają własną logikę).
    3.  **Dla stron chronionych:**
        *   Próbuje odczytać ciasteczko HTTP-only przechowujące token (np. `session_token`).
        *   **Jeśli ciasteczko `session_token` istnieje:** Middleware **zakłada**, że użytkownik *może* być zalogowany i **pozwala na dostęp** do strony, kontynuując obsługę żądania (`next()`). **Nie wykonuje zapytania do backendu w celu weryfikacji tokena.**
        *   **Jeśli ciasteczko `session_token` nie istnieje:** Middleware bezpośrednio przekierowuje użytkownika na `/login` (`return Astro.redirect('/login')`).
*   **Konsekwencje i Kompromisy:**
    *   **Brak współdzielonego sekretu JWT:** Astro nie musi znać sekretu.
    *   **Szybkie ładowanie stron:** Brak dodatkowego zapytania do backendu przy dostępie do strony chronionej.
    *   **Ryzyko "Mignięcia" Treści:** Użytkownik z nieważnym lub wygasłym tokenem w ciasteczku uzyska dostęp do strony chronionej. Dopiero próba wykonania akcji (np. pobrania danych przez API) na tej stronie ujawni nieważność sesji (API Astro przekaże token do Springa, Spring zwróci 401, API Astro przekaże 401 do frontendu).
    *   **Odpowiedzialność Frontendu:** Logika po stronie klienta (np. w hookach do zapytań API, globalnym obsłudze błędów) **musi** być przygotowana na otrzymywanie błędów 401 Unauthorized z API i odpowiednio reagować (np. czyszcząc stan lokalny, przekierowując użytkownika na `/login`).
    *   **Brak `Astro.locals.user` z Middleware:** Ponieważ middleware nie weryfikuje sesji, nie może w niezawodny sposób pobrać i ustawić danych użytkownika w `Astro.locals.user`. Dane użytkownika do personalizacji UI muszą być pobierane przez dedykowane zapytania API po stronie klienta lub przekazywane w inny sposób.

### 2.2. Endpointy API (Astro API Routes)

*   **`POST /api/auth/login.ts` (`src/pages/api/auth/login.ts`):**
    *   **Cel:** Obsługa logowania, pośredniczenie z backendem Spring, ustawienie ciasteczka z JWT.
    *   **Logika:**
        1.  Odbiera `name`, `password`.
        2.  Wywołuje `POST /api/auth/login` w Springu.
        3.  Obsługuje odpowiedź:
            *   **Sukces (Spring zwraca `JwtResponse`):**
                *   Odbiera `accessToken`.
                *   **Umieszcza otrzymany `accessToken`** w bezpiecznym, HTTP-only ciasteczku (np. `session_token`) z odpowiednimi atrybutami (`Secure`, `SameSite`, `Max-Age`/`Expires`). **Astro nie weryfikuje tego tokena.**
                *   Zwraca `200 OK` (opcjonalnie z `UserDto`) do frontendu.
            *   **Błąd:** Przekazuje błąd z backendu do frontendu.

*   **`POST /api/auth/register.ts` (`src/pages/api/auth/register.ts`):**
    *   **Cel:** Obsługa rejestracji użytkownika, pośredniczenie między formularzem React a backendem Spring.
    *   **Logika:**
        1.  Odbiera dane (`name`, `password`, `passwordConfirmation`) z ciała żądania.
        2.  Przeprowadza walidację serwerową (zgodność haseł, długości, itp.).
        3.  Wywołuje endpoint backendu Spring: `POST /api/auth/register` z danymi.
        4.  Obsługuje odpowiedź z backendu:
            *   **Sukces (Spring zwraca 200/201 z `UserDto` lub `ApiResponse`):** Zwraca odpowiedź `200 OK` lub `201 Created` do frontendu z odpowiednim ciałem.
            *   **Błąd (Spring zwraca 400, 409, itp.):** Przekazuje status błędu i komunikat/ciało błędu z backendu do frontendu.
            *   **Błąd połączenia z backendem:** Zwraca `500` lub `503`.

*   **`POST /api/auth/logout.ts` (`src/pages/api/auth/logout.ts`):**
    *   **Cel:** Wylogowanie użytkownika.
    *   **Logika:**
        1.  **Usuwa ciasteczko** `session_token` (ustawiając pustą wartość i przeszłą datę wygaśnięcia).
        2.  (Opcjonalnie) Wywołuje `POST /api/auth/logout` w Springu (jeśli jest potrzebne np. do blacklistowania).
        3.  Zwraca `200 OK`.

*   **Inne Endpointy API Astro działające jako Proxy (np. `GET /api/characters.ts`, `POST /api/dialogues/generate.ts`):**
    *   **Cel:** Przekazywanie żądań do chronionych endpointów Springa, dodając uwierzytelnienie.
    *   **Logika:**
        1.  Odbierają żądanie od klienta.
        2.  **Odczytują token JWT** z przychodzącego ciasteczka `session_token`.
        3.  Jeśli ciasteczko istnieje:
            *   Przygotowują żądanie do odpowiedniego endpointu Springa.
            *   **Dołączają odczytany token** do nagłówka `Authorization: Bearer <token_z_ciasteczka>` żądania wychodzącego do Springa.
            *   Wysyłają żądanie do Springa.
            *   Przekazują odpowiedź (sukces lub błąd) od Springa z powrotem do klienta.
        4.  Jeśli ciasteczko nie istnieje: Zwracają błąd `401 Unauthorized` do klienta.

### 2.3. Konfiguracja

*   **Sekret JWT:** Sekret używany do podpisywania tokenów JWT **pozostaje wyłącznie w konfiguracji backendu Spring**. Astro nie musi go znać.
*   **Adres URL Backendu:** Adres URL backendu Spring musi być skonfigurowany w Astro (np. przez zmienne środowiskowe) do wywołań API.
*   **Nazwa Ciasteczka:** Należy ustalić spójną nazwę dla ciasteczka przechowującego JWT (np. `session_token`).
*   ~~**Endpoint Weryfikacji Sesji:**~~ W tym podejściu middleware nie potrzebuje dedykowanego endpointu do weryfikacji sesji.

### 2.4. Obsługa Wyjątków

*   Endpointy API w Astro powinny zawierać bloki `try...catch` do obsługi błędów walidacji, błędów komunikacji z backendem Spring oraz innych nieoczekiwanych wyjątków, zwracając odpowiednie statusy HTTP i komunikaty błędów w formacie JSON.

## 3. Kluczowe Wnioski i Zgodność

*   Architektura wykorzystuje Astro jako warstwę pośredniczącą (API Gateway), która zarządza przepływem uwierzytelniania i przekazuje odpowiedzialność za weryfikację JWT do backendu Spring.
*   **Sekret JWT nie jest współdzielony.**
*   JWT jest bezpiecznie przechowywany w ciasteczku HTTP-only po stronie klienta.
*   Middleware Astro chroni **strony** w **uproszczony sposób**, bazując jedynie na **obecności** ciasteczka sesji. Nie wykonuje dodatkowych zapytań weryfikacyjnych do backendu, co przyspiesza ładowanie stron, ale wprowadza ryzyko "mignięcia" chronionej treści.
*   Pełna autoryzacja dzieje się w backendzie Spring, wywoływanym przez API Astro; logika frontendowa musi obsługiwać błędy 401.
*   Endpointy API Astro działające jako proxy **przekazują** token z ciasteczka do backendu Spring w nagłówku `Authorization`.
*   Komponenty React (`LoginForm`, `RegisterForm`) hermetyzują logikę formularzy, walidacji client-side i komunikacji z API Astro.
*   Layout Astro dynamicznie dostosowuje UI na podstawie stanu uwierzytelnienia.
*   Rozwiązanie jest zgodne z wymaganiami US-001, US-002 z `@prd.md`, wykorzystuje technologie z `@tech-stack.md` i integruje się z istniejącym planem UI oraz specyfikacją backendu `@be-auth-spec.md`.
*   Nie narusza istniejącej funkcjonalności aplikacji; jedynie rozszerza ją o warstwę uwierzytelniania i autoryzacji. Strony i API niezwiązane z auth będą działać jak dotychczas, a te wymagające autoryzacji będą chronione przez middleware. 