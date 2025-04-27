# Architektura UI dla d-AI-logi

## 1. Przegląd struktury UI

Architektura interfejsu użytkownika (UI) dla aplikacji d-AI-logi opiera się na frameworku Astro 5 z wykorzystaniem React 19 do tworzenia interaktywnych komponentów ("wysp"). Stylizacja realizowana jest za pomocą Tailwind 4, a gotowe komponenty UI pochodzą z biblioteki Shadcn/ui. Aplikacja działa jako Single Page Application (SPA) z routingiem zarządzanym przez Astro.

Struktura UI dzieli się na dwie główne części:
1.  **Strefa Publiczna:** Dostępna bez logowania, obejmująca stronę główną (`/`), strony logowania (`/login`) i rejestracji (`/register`), a także publiczny widok listy postaci globalnych (`/characters`).
2.  **Strefa Uwierzytelniona:** Dostępna po zalogowaniu, chroniona przez middleware Astro, obejmująca wszystkie główne funkcjonalności aplikacji (panel główny `/dashboard`, zarządzanie postaciami, scenami, profilem).

Nawigacja w strefie uwierzytelnionej odbywa się za pomocą stałego paska bocznego lub nagłówka (`PublicLayout`). Do zarządzania stanem globalnym (dane użytkownika, stan uwierzytelnienia, lista LLM) wykorzystywana jest biblioteka Zustand. Uwierzytelnianie opiera się na sesjach zarządzanych po stronie serwera przez middleware Astro, z wykorzystaniem bezpiecznych ciasteczek HTTP-only do przechowywania identyfikatora sesji (lub samego JWT). Komunikacja z backendem (API endpoints w Astro) odbywa się automatycznie z wykorzystaniem ciasteczka sesyjnego, a funkcje pomocnicze po stronie klienta obsługują podstawowe błędy API (np. 401 - nieautoryzowany dostęp, 422, inne błędy ogólne). Architektura jest zaprojektowana z myślą o widoku desktopowym (desktop-first) zgodnie z wymaganiami MVP.

## 2. Lista widoków

### Widoki Publiczne

*   **Nazwa widoku:** Strona Główna (Publiczna)
    *   **Ścieżka widoku:** `/`
    *   **Główny cel:** Przedstawienie aplikacji nowym użytkownikom, zachęcenie do rejestracji/logowania.
    *   **Kluczowe informacje do wyświetlenia:** Opis aplikacji, jej możliwości, przyciski "Zaloguj się" i "Zarejestruj się", link do przykładowej (mockowej) sceny (lub jej wizualizacja).
    *   **Kluczowe komponenty widoku:** Komponenty tekstowe, `Button` ("Zaloguj się", "Zarejestruj się"), Link/Wizualizacja Przykładowej Sceny.
    *   **UX, dostępność i względy bezpieczeństwa:** Jasny opis wartości aplikacji. Dostępny bez logowania.

*   **Nazwa widoku:** Logowanie
    *   **Ścieżka widoku:** `/login`
    *   **Główny cel:** Umożliwienie istniejącym użytkownikom zalogowania się do aplikacji.
    *   **Kluczowe informacje do wyświetlenia:** Formularz logowania.
    *   **Kluczowe komponenty widoku:** `Input` ("Nazwa użytkownika", "Hasło"), `Button` ("Zaloguj się"), Link do `/register` ("Nie masz konta? Zarejestruj się").
    *   **UX, dostępność i względy bezpieczeństwa:** Walidacja po stronie klienta (minimalna długość), obsługa błędów logowania (400 - "Nieprawidłowe dane logowania"), użycie `Input type="password"`.

*   **Nazwa widoku:** Rejestracja
    *   **Ścieżka widoku:** `/register`
    *   **Główny cel:** Umożliwienie nowym użytkownikom założenia konta.
    *   **Kluczowe informacje do wyświetlenia:** Formularz rejestracji.
    *   **Kluczowe komponenty widoku:** `Input` ("Nazwa użytkownika", "Hasło", "Potwierdź hasło"), `Button` ("Zarejestruj się"), Link do `/login` ("Masz już konto? Zaloguj się").
    *   **UX, dostępność i względy bezpieczeństwa:** Walidacja po stronie klienta (minimalna długość hasła, zgodność haseł), obsługa błędów (400 - "Nieprawidłowe dane", 409 - "Nazwa użytkownika zajęta"), użycie `Input type="password"`.

*   **Nazwa widoku:** Lista Postaci (Publiczna/Globalne)
    *   **Ścieżka widoku:** `/characters` (dla niezalogowanych)
    *   **Główny cel:** Wyświetlenie listy globalnych postaci dostępnych w aplikacji.
    *   **Kluczowe informacje do wyświetlenia:** Lista postaci globalnych (awatar, nazwa, krótki opis), paginacja.
    *   **Kluczowe komponenty widoku:** `Card` (dla każdej postaci), `Avatar`, `Button` ("Zobacz szczegóły"), `Pagination`. Komunikat o pustym stanie ("Brak postaci globalnych").
    *   **UX, dostępność i względy bezpieczeństwa:** Widok tylko do odczytu. Paginacja. Dostępny bez logowania.

*   **Nazwa widoku:** Podgląd Postaci Globalnej
    *   **Ścieżka widoku:** `/characters/[id]` (dla niezalogowanych)
    *   **Główny cel:** Wyświetlenie szczegółów postaci globalnej.
    *   **Kluczowe informacje do wyświetlenia:** Nazwa, Awatar, Krótki opis, Pełny opis, Domyślny LLM (jeśli ustawiony).
    *   **Kluczowe komponenty widoku:** `Avatar`, Komponenty tekstowe do wyświetlania informacji.
    *   **UX, dostępność i względy bezpieczeństwa:** Widok tylko do odczytu. Dostępność zależy od implementacji backendu (`GET /api/characters/{id}` dla `is_global=true` musi być publiczny).

### Widoki Uwierzytelnione

*   **Nazwa widoku:** Panel Główny (Dashboard)
    *   **Ścieżka widoku:** `/dashboard`
    *   **Główny cel:** Strona startowa po zalogowaniu, zapewniająca nawigację do głównych sekcji.
    *   **Kluczowe informacje do wyświetlenia:** Powitanie ("Witaj, [nazwa_użytkownika]!"), linki/karty nawigacyjne.
    *   **Kluczowe komponenty widoku:** Karty lub linki do: "Postaci", "Historia Scen", "Nowa Scena", "Profil".
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware.

*   **Nazwa widoku:** Profil Użytkownika
    *   **Ścieżka widoku:** `/profile`
    *   **Główny cel:** Zarządzanie kluczem API OpenRouter użytkownika.
    *   **Kluczowe informacje do wyświetlenia:** Stan zapisania klucza API, formularz do wprowadzenia/aktualizacji klucza.
    *   **Kluczowe komponenty widoku:** `Input type="password"` ("Klucz API OpenRouter"), `Button` ("Zapisz klucz"), `Button` ("Usuń klucz", jeśli klucz istnieje), Wskaźnik wizualny `has_api_key` ("Klucz API zapisany" / "Brak zapisanego klucza API").
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Klucz API maskowany w inpucie. Obsługa błędów zapisu/usuwania (`Toast`).

*   **Nazwa widoku:** Lista Postaci (Zalogowany)
    *   **Ścieżka widoku:** `/characters` (dla zalogowanych)
    *   **Główny cel:** Wyświetlenie listy postaci użytkownika oraz globalnych, umożliwienie zarządzania własnymi postaciami.
    *   **Kluczowe informacje do wyświetlenia:** Połączona lista postaci (własne i globalne, z wyraźnym oznaczeniem), opcje zarządzania (edycja/usuwanie dla własnych), paginacja.
    *   **Kluczowe komponenty widoku:** `Card` (dla każdej postaci, z oznaczeniem "Globalna"/"Twoja"), `Avatar`, `Button` ("Stwórz nową", "Edytuj", "Usuń", "Zobacz szczegóły"), `Pagination`. Komunikat o pustym stanie ("Nie masz jeszcze żadnych postaci. Stwórz pierwszą!").
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Potwierdzenie usunięcia (`AlertDialog` - "Czy na pewno chcesz usunąć postać [nazwa]?"). Paginacja. Wyraźne rozróżnienie postaci własnych i globalnych.

*   **Nazwa widoku:** Tworzenie Postaci
    *   **Ścieżka widoku:** `/characters/new`
    *   **Główny cel:** Formularz do tworzenia nowej postaci AI przez zalogowanego użytkownika.
    *   **Kluczowe informacje do wyświetlenia:** Pola formularza (Nazwa, Opis, Krótki opis), Opcja wgrania awatara, Opcja wyboru domyślnego LLM.
    *   **Kluczowe komponenty widoku:** `Form` (`react-hook-form`), `Input` ("Nazwa", "Krótki opis"), `Textarea` ("Opis"), Komponent do uploadu awatara (z walidacją typu, rozmiaru, wymiarów i podglądem), `Select` ("Domyślny model LLM" - opcjonalnie), `Button` ("Zapisz postać", "Anuluj").
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Walidacja po stronie klienta (zgodna z API). Obsługa błędu 422 (limit postaci) przez `AlertDialog` ("Osiągnięto limit 50 postaci.").

*   **Nazwa widoku:** Podgląd Postaci (Własnej)
    *   **Ścieżka widoku:** `/characters/[id]` (dla zalogowanych, gdy postać jest własna)
    *   **Główny cel:** Wyświetlenie szczegółów postaci należącej do użytkownika, z opcjami edycji/usunięcia.
    *   **Kluczowe informacje do wyświetlenia:** Nazwa, Awatar, Krótki opis, Pełny opis, Domyślny LLM, Przyciski akcji.
    *   **Kluczowe komponenty widoku:** `Avatar`, Komponenty tekstowe, `Button` ("Edytuj", "Usuń").
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Potwierdzenie usunięcia (`AlertDialog`).

*   **Nazwa widoku:** Edycja Postaci
    *   **Ścieżka widoku:** `/characters/[id]/edit`
    *   **Główny cel:** Formularz do edycji istniejącej postaci użytkownika.
    *   **Kluczowe informacje do wyświetlenia:** Wypełnione pola formularza (jak przy tworzeniu), opcje zarządzania awatarem.
    *   **Kluczowe komponenty widoku:** `Form` (`react-hook-form`), Wypełnione `Input`, `Textarea`, Komponent do uploadu/usuwania awatara, `Select` ("Domyślny model LLM"), `Button` ("Zapisz zmiany", "Anuluj").
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Walidacja po stronie klienta.

*   **Nazwa widoku:** Historia Scen
    *   **Ścieżka widoku:** `/scenes`
    *   **Główny cel:** Wyświetlenie listy zapisanych scen użytkownika.
    *   **Kluczowe informacje do wyświetlenia:** Lista scen (Nazwa, Uczestnicy, Data), Paginacja.
    *   **Kluczowe komponenty widoku:** Lista (np. tabelaryczna lub karty), `Button` ("Zobacz", "Usuń"), `Pagination`. Komunikat o pustym stanie ("Brak zapisanych scen.").
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Potwierdzenie usunięcia (`AlertDialog` - "Czy na pewno chcesz usunąć scenę [nazwa]?"). Paginacja.

*   **Nazwa widoku:** Podgląd Zapisanej Sceny
    *   **Ścieżka widoku:** `/scenes/[id]`
    *   **Główny cel:** Wyświetlenie pełnej treści zapisanej sceny.
    *   **Kluczowe informacje do wyświetlenia:** Nazwa sceny, Opis sceny, Konfiguracja postaci (Awatar, Nazwa, użyty LLM), Wiadomości sceny (tura po turze).
    *   **Kluczowe komponenty widoku:** Komponenty tekstowe, Lista konfiguracji postaci, Komponent wyświetlający chat (dymki z `Avatar`, nazwą, treścią).
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Widok tylko do odczytu.

*   **Nazwa widoku:** Nowa Scena
    *   **Ścieżka widoku:** `/scenes/new`
    *   **Główny cel:** Umożliwienie użytkownikowi skonfigurowania parametrów nowej sceny i zainicjowania generowania sceny, a następnie wyświetlenie wyniku i opcji zapisu.
    *   **Kluczowe informacje do wyświetlenia:**
        *   **Faza Konfiguracji:** Formularz ("Opis sceny", Wybór 2-3 postaci, Wybór LLM dla każdej postaci).
        *   **Faza Generowania:** Wskaźnik ładowania.
        *   **Faza Wyniku:** Wygenerowana scena lub komunikat o błędzie. Sekcja zapisu sceny ("Nazwa sceny" + przycisk "Zapisz scenę").
    *   **Kluczowe komponenty widoku:**
        *   **Faza Konfiguracji:** `Form`, `Textarea` ("Opis sceny"), 2-3x `Select` ("Wybierz postać" - z awatarem i nazwą w opcjach), 2-3x `Select` ("Wybierz model LLM"), `Button` ("Rozpocznij scenę").
        *   **Faza Generowania:** `Progress` / `Spinner`.
        *   **Faza Wyniku:** Komponent wyświetlający chat, `Input` ("Nazwa sceny" - pre-filled), `Button` ("Zapisz scenę"). Komunikat o błędzie (jeśli wystąpił).
    *   **UX, dostępność i względy bezpieczeństwa:** Chronione przez middleware. Walidacja (wymagany opis sceny, 2-3 postacie, wybrane LLM). Obsługa stanu ładowania. Obsługa błędów generowania (w tym 402 - "Brak klucza API OpenRouter lub niewystarczające środki na koncie globalnym." - dla zwykłych użytkowników/specjalnych bez klucza). Obsługa błędu 422 (limit scen) przy zapisie przez `AlertDialog` ("Osiągnięto limit 50 zapisanych scen."). Domyślna sugestia nazwy sceny ("Rozmowa |Postać1|Postać 2|Postać3| - Data").

## 3. Mapa podróży użytkownika

**Główny przepływ: Tworzenie i zapisywanie sceny**

1.  Użytkownik loguje się (`/login`) i trafia na Panel Główny (`/dashboard`).
2.  Z Panelu Głównego klika "Nowa Scena", przechodząc do `/scenes/new`.
3.  W widoku Tworzenia Nowej Sceny:
    *   Wpisuje opis sceny w `Textarea`.
    *   Wybiera 2 lub 3 postacie z rozwijanych list `Select`. Lista zawiera postacie użytkownika i globalne (pobierane z `GET /api/characters`).
    *   Dla każdej wybranej postaci wybiera model LLM z listy `Select` (pobierane z `GET /api/llms`, domyślnie wybrany, jeśli postać ma `default_llm_id`).
4.  Klika przycisk "Rozpocznij scenę". Przycisk staje się nieaktywny, pojawia się wskaźnik ładowania.
5.  Aplikacja wysyła żądanie `POST /api/dialogues/generate`.
6.  Aplikacja czeka na synchroniczną odpowiedź (JSON ze sceną lub błąd).
7.  Widok `/scenes/new` aktualizuje się:
    *   **Sukces:** Wskaźnik ładowania znika. Pojawia się wygenerowana scena (lista wiadomości w dymkach). Poniżej pojawia się sekcja "Zapisz scenę" z polem `Input` (wypełnionym sugerowaną nazwą) i przyciskiem "Zapisz scenę".
    *   **Błąd:** Wskaźnik ładowania znika. Pojawia się komunikat o błędzie (np. `Toast` dla błędów ogólnych, informacja w widoku dla błędów API OpenRouter) oraz ewentualnie częściowo wygenerowana scena.
8.  W przypadku sukcesu, użytkownik może edytować nazwę sceny i kliknąć "Zapisz scenę".
9.  Aplikacja wysyła żądanie `POST /api/dialogues` z pełnym obiektem sceny (konfiguracja, wiadomości, status, opis) i nazwą podaną przez użytkownika.
10. Aplikacja reaguje na odpowiedź:
    *   **Sukces:** Wyświetla `Toast` potwierdzający zapis ("Scena zapisana pomyślnie"). Może przekierować do `/scenes` lub `/scenes/[id_nowej_sceny]`.
    *   **Błąd 422:** Wyświetla `AlertDialog` informujący o osiągnięciu limitu zapisanych scen.
    *   **Inny błąd:** Wyświetla `Toast` z informacją o błędzie zapisu.

**Inne kluczowe przepływy:**

*   **Publiczny dostęp:** ` / ` -> (`/login` | `/register` | `/characters` (tylko globalne) | `/characters/[id]` (tylko globalne))
*   **Rejestracja/Logowanie:** `/register` -> `/login` -> `/dashboard`
*   **Zarządzanie postaciami:** `/dashboard` -> `/characters` -> (Create:`/characters/new` | View:`/characters/[id]` | Edit:`/characters/[id]/edit` | Delete: `AlertDialog`)
*   **Przeglądanie historii:** `/dashboard` -> `/scenes` -> (`/scenes/[id]` | Delete: `AlertDialog`)
*   **Zarządzanie kluczem API:** `/dashboard` -> `/profile`

## 4. Układ i struktura nawigacji

*   **Główny Layout Astro (`Layout.astro`):**
    *   Odpowiada za podstawową strukturę HTML (`<head>`, `<body>`), wczytywanie globalnych stylów i skryptów.
    *   Zawiera stały element nawigacyjny (np. nagłówek lub pasek boczny).
    *   **Warunkowe renderowanie:** Na podstawie danych sesji użytkownika (dostępnych np. przez `Astro.locals.user` z middleware), layout dynamicznie pokazuje:
        *   **Dla niezalogowanych:** Linki/przyciski "Zaloguj się" i "Zarejestruj się".
        *   **Dla zalogowanych:** Główną nawigację aplikacji, nazwę użytkownika i przycisk "Wyloguj".
*   **Nawigacja Główna (w `Layout.astro`, widoczna dla zalogowanych):**
    *   Logo / Nazwa Aplikacji (link do `/dashboard`)
    *   Postaci (link do `/characters`)
    *   Historia Scen (link do `/scenes`)
    *   Nowa Scena (link do `/scenes/new`)
    *   Profil (link do `/profile`)
    *   Nazwa użytkownika (wyświetlana)
    *   Przycisk Wyloguj (wywołuje endpoint API do zakończenia sesji po stronie serwera, np. `POST /api/auth/logout`, czyści stan globalny, przekierowuje do `/`)
*   **Middleware (`src/middleware/index.ts`):**
    *   Przechwytuje żądania do chronionych ścieżek (np. `/dashboard`, `/profile`, `/scenes/**`, `/characters/new`, `/characters/[id]/edit`).
    *   Weryfikuje obecność i ważność ciasteczka sesji.
    *   Jeśli sesja jest nieprawidłowa lub jej brak, przekierowuje na `/login`.
    *   Obsługuje proces logowania/rejestracji: po pomyślnej weryfikacji danych uwierzytelniających, tworzy sesję i ustawia bezpieczne ciasteczko HTTP-only.
    *   Udostępnia dane sesji (np. ID użytkownika) kontekstowi żądania dla punktów końcowych API i stron Astro.
    *   Ścieżki publiczne (`/`, `/login`, `/register`) są wykluczone ze sprawdzania sesji.
*   **Komponenty Astro (`.astro` pages/layouts):** Mogą uzyskiwać dostęp do danych sesji (np. `Astro.locals.user`) udostępnionych przez middleware do personalizacji interfejsu (np. wyświetlanie nazwy użytkownika).

## 5. Kluczowe komponenty (Shadcn/ui & inne)

*   **`Button`:** Do wszystkich akcji klikalnych.
*   **`Input`:** Do wprowadzania tekstu (w tym `type="password"`).
*   **`Textarea`:** Do wprowadzania dłuższego tekstu.
*   **`Card`:** Do wyświetlania elementów list (postaci).
*   **`Select`:** Do wyboru postaci i LLM. Wymaga dostosowania do wyświetlania awatara w opcjach postaci.
*   **`Avatar`:** Do wyświetlania awatarów postaci (z `AvatarFallback` jako placeholder).
*   **`AlertDialog`:** Do potwierdzeń (usuwanie) i krytycznych powiadomień (limity zasobów - np. "Osiągnięto limit postaci/scen.").
*   **`Toast` / `Toaster`:** Do nieinwazyjnych powiadomień o sukcesie/błędzie.
*   **`Pagination`:** Do nawigacji po paginowanych listach.
*   **`Form` (react-hook-form):** Do zarządzania stanem i walidacją formularzy.
*   **`Label`:** Do etykietowania pól formularzy.
*   **Komponent Wyświetlania Sceny:** Niestandardowy komponent React do renderowania wiadomości sceny w formie dymków z awatarem i nazwą.
*   **Komponent Uploadu Awatara:** Niestandardowy komponent React obsługujący wybór pliku, podgląd, walidację (typ, rozmiar, wymiary) i komunikację z API (`POST /api/characters/{id}/avatar`).
*   **Wskaźnik Ładowania (`Spinner`/`Progress`):** Do sygnalizowania operacji w tle (np. generowanie sceny).
*   **Layouty (`BaseLayout`, `PublicLayout`, `AppLayout`):** Komponenty Astro definiujące strukturę strony.
*   **Hooki/Funkcje Pomocnicze API (frontend):** Zestaw funkcji do komunikacji z punktami końcowymi API Astro (`/api/*`), obsługujących podstawową obsługę błędów (np. wyświetlanie `Toast` dla błędów 4xx/5xx). Nie muszą już zarządzać tokenem JWT.
*   **Globalny Store (Zustand):** Do przechowywania stanu uwierzytelnienia (`isLoggedIn`), danych użytkownika pobranych z API, listy LLM. Stan `isLoggedIn` jest synchronizowany na podstawie dostępności danych sesji lub odpowiedzi API. 