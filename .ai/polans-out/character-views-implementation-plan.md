# Plan implementacji widoków postaci (Tworzenie, Podgląd, Edycja)

## 1. Przegląd
Ten plan opisuje implementację trzech powiązanych widoków do zarządzania postaciami AI w aplikacji d-AI-logi:
*   **Tworzenie Postaci:** Formularz umożliwiający zalogowanym użytkownikom dodawanie nowych postaci.
*   **Podgląd Postaci:** Wyświetlanie szczegółów istniejącej postaci należącej do użytkownika, z opcjami edycji i usunięcia.
*   **Edycja Postaci:** Formularz umożliwiający modyfikację istniejącej postaci użytkownika.

Implementacja wykorzysta Astro dla struktury stron i React (z TypeScript) dla interaktywnych komponentów formularzy i wyświetlania danych, zgodnie ze stackiem technologicznym projektu (Astro 5, React 19, TypeScript 5, Tailwind 4, Shadcn/ui). Widoki będą chronione i dostępne tylko dla zalogowanych użytkowników.

## 2. Routing widoku
*   **Tworzenie Postaci:** `/characters/new`
*   **Podgląd Postaci:** `/characters/[id]` (gdzie `[id]` to dynamiczny parametr ID postaci)
*   **Edycja Postaci:** `/characters/[id]/edit` (gdzie `[id]` to dynamiczny parametr ID postaci)

Wszystkie ścieżki będą chronione przez middleware Astro (`src/middleware/index.ts`), weryfikujące status zalogowania użytkownika.

## 3. Struktura komponentów
```
// Strona Astro: /characters/new
src/pages/characters/new.astro
└── CharacterFormWrapper (React Client Component) // Pobiera LLM, obsługuje anulowanie/sukces
    └── CharacterForm (React Component)
        ├── Shadcn Form (react-hook-form)
        │   ├── Shadcn Input (Nazwa)
        │   ├── Shadcn Input (Krótki opis)
        │   ├── Shadcn Textarea (Opis)
        │   └── Shadcn Select (Domyślny LLM)
        ├── AvatarUploader (React Component)
        │   ├── Shadcn Avatar (Podgląd)
        │   ├── Shadcn Button (Wybierz/Zmień)
        │   └── Shadcn Button (Usuń - tylko w edycji)
        ├── Shadcn Button (Zapisz postać)
        └── Shadcn Button (Anuluj)
        └── Shadcn AlertDialog (Dla błędu 422 - limit postaci)

// Strona Astro: /characters/[id].astro
src/pages/characters/[id].astro
└── CharacterDetailsWrapper (React Client Component) // Pobiera dane postaci i LLM, obsługuje nawigację/usuwanie
    └── CharacterDetails (React Component)
        ├── Shadcn Avatar
        ├── Komponenty tekstowe (Nazwa, Opisy, LLM)
        ├── Shadcn Button (Edytuj)
        └── Shadcn Button (Usuń)
        └── DeleteConfirmationDialog (Shadcn AlertDialog) // Potwierdzenie usunięcia

// Strona Astro: /characters/[id]/edit.astro
src/pages/characters/[id]/edit.astro
└── CharacterFormWrapper (React Client Component) // Pobiera dane postaci i LLM, obsługuje anulowanie/sukces
    └── CharacterForm (React Component) // Ten sam co przy tworzeniu, ale z initialData
        ├── Shadcn Form (react-hook-form) // Pola wypełnione danymi
        │   ├── Shadcn Input (Nazwa)
        │   ├── Shadcn Input (Krótki opis)
        │   ├── Shadcn Textarea (Opis)
        │   └── Shadcn Select (Domyślny LLM)
        ├── AvatarUploader (React Component) // Z initialAvatarUrl
        │   ├── Shadcn Avatar (Podgląd)
        │   ├── Shadcn Button (Wybierz/Zmień)
        │   └── Shadcn Button (Usuń)
        ├── Shadcn Button (Zapisz zmiany)
        └── Shadcn Button (Anuluj)
```

Komponenty React (`CharacterFormWrapper`, `CharacterDetailsWrapper`) będą osadzone w stronach Astro z dyrektywą `client:load` lub `client:visible`, aby umożliwić interaktywność po stronie klienta. Wrapper Components (`CharacterFormWrapper`, `CharacterDetailsWrapper`) będą odpowiedzialne za pobieranie danych (lub przyjmowanie ich z Astro) i zarządzanie logiką nawigacji/stanu na poziomie widoku.

## 4. Szczegóły komponentów

### `CharacterFormWrapper` (React Client Component - `.tsx`)
*   **Opis:** Komponent-wrapper dla widoków tworzenia i edycji postaci. Odpowiedzialny za pobranie listy LLM, pobranie danych postaci (w trybie edycji), inicjalizację `CharacterForm` oraz obsługę nawigacji po zapisaniu/anulowaniu.
*   **Główne elementy:** Zawiera komponent `CharacterForm`. Może renderować stan ładowania/błędu podczas pobierania danych.
*   **Obsługiwane interakcje:** Przekazuje callbacki `onSubmitSuccess` i `onCancel` do `CharacterForm`.
*   **Obsługiwana walidacja:** Brak bezpośredniej walidacji.
*   **Typy:** `LLMDTO[]`, `CharacterDTO` (dla trybu edycji).
*   **Propsy:** `characterId?: number` (z Astro params dla trybu edycji).

### `CharacterForm` (React Component - `.tsx`)
*   **Opis:** Główny formularz do tworzenia/edycji postaci. Wykorzystuje `react-hook-form` do zarządzania stanem formularza i walidacji. Integruje komponent `AvatarUploader`. Wywołuje odpowiednie API przy submisji.
*   **Główne elementy:** `form` (z `react-hook-form`), `Input` (Shadcn), `Textarea` (Shadcn), `Select` (Shadcn dla LLM), `AvatarUploader`, `Button` (Shadcn - Zapisz/Anuluj), `AlertDialog` (Shadcn - dla błędu 422).
*   **Obsługiwane interakcje:** Wprowadzanie danych w polach, wybór LLM, wybór/usunięcie awatara, submit formularza, anulowanie.
*   **Obsługiwana walidacja (zgodnie z API):**
    *   `name`: Wymagane, min 1 znak, max 100 znaków.
    *   `short_description`: Wymagane, min 0 znaków, max 500 znaków.
    *   `description`: Wymagane, min 0 znaków, max 5000 znaków.
    *   `avatar`: Opcjonalne. Jeśli wybrano plik: typ (`image/png` lub `image/jpeg`), rozmiar (max 1MB), wymiary (dokładnie 256x256px).
*   **Typy:** `CharacterFormViewModel`, `LLMDTO[]`, `CharacterDTO` (dla `initialData`).
*   **Propsy:**
    *   `characterId?: number` (Opcjonalne, tryb edycji)
    *   `initialData?: CharacterDTO` (Opcjonalne, dane do wypełnienia w trybie edycji)
    *   `llms: LLMDTO[]` (Lista dostępnych modeli LLM)
    *   `onSubmitSuccess: (character: CharacterDTO) => void` (Callback po pomyślnym zapisie)
    *   `onCancel: () => void` (Callback przy anulowaniu)
    *   `isSubmitting?: boolean` (Opcjonalne, zarządzane zewnętrznie jeśli używamy `useMutation`)

### `AvatarUploader` (React Component - `.tsx`)
*   **Opis:** Komponent do zarządzania awatarem postaci. Umożliwia wybór pliku, walidację (typ, rozmiar, wymiary), podgląd oraz usunięcie (w trybie edycji).
*   **Główne elementy:** `Avatar` (Shadcn - podgląd), `Button` (Shadcn - wybierz/zmień plik), ukryty `input type="file"`, `Button` (Shadcn - usuń, tylko w edycji i gdy jest awatar), komunikaty o błędach walidacji.
*   **Obsługiwane interakcje:** Kliknięcie przycisku wyboru pliku, wybór pliku w oknie systemowym, kliknięcie przycisku usunięcia awatara.
*   **Obsługiwana walidacja:**
    *   Typ pliku: `image/png` lub `image/jpeg`.
    *   Rozmiar pliku: Maksymalnie 1MB (1024 * 1024 bajtów).
    *   Wymiary obrazu: Dokładnie 256x256 pikseli (sprawdzane asynchronicznie po stronie klienta).
*   **Typy:** `File` object.
*   **Propsy:**
    *   `initialAvatarUrl?: string` (URL istniejącego awatara w trybie edycji)
    *   `onAvatarChange: (file: File | null) => void` (Callback zwracający wybrany i zwalidowany plik lub `null` jeśli awatar ma być usunięty/nieustawiony)
    *   `characterId?: number` (Opcjonalne, potrzebne do ewentualnego wywołania DELETE `/api/characters/{id}/avatar`)

### `CharacterDetailsWrapper` (React Client Component - `.tsx`)
*   **Opis:** Komponent-wrapper dla widoku szczegółów postaci. Odpowiedzialny za pobranie danych postaci i listy LLM, obsługę stanu ładowania/błędu, inicjalizację `CharacterDetails` oraz logikę nawigacji do edycji i usuwania.
*   **Główne elementy:** Zawiera komponent `CharacterDetails`. Może renderować stan ładowania/błędu.
*   **Obsługiwane interakcje:** Przekazuje ID postaci do `CharacterDetails`, obsługuje nawigację (`onEdit`) i proces usuwania (`onDelete`).
*   **Obsługiwana walidacja:** Brak.
*   **Typy:** `CharacterDTO`, `LLMDTO[]`.
*   **Propsy:** `characterId: number` (z Astro params).

### `CharacterDetails` (React Component - `.tsx`)
*   **Opis:** Wyświetla szczegółowe informacje o postaci: awatar, nazwę, opisy, domyślny model LLM. Udostępnia przyciski akcji "Edytuj" i "Usuń". Integruje `DeleteConfirmationDialog`.
*   **Główne elementy:** `Avatar` (Shadcn), `h1`, `p` (dla nazw, opisów, LLM), `Button` (Shadcn - "Edytuj", "Usuń"), `DeleteConfirmationDialog`.
*   **Obsługiwane interakcje:** Kliknięcie przycisku "Edytuj", kliknięcie przycisku "Usuń" (otwiera dialog potwierdzenia).
*   **Obsługiwana walidacja:** Brak.
*   **Typy:** `CharacterDTO`, `LLMDTO[]`.
*   **Propsy:**
    *   `character: CharacterDTO` (Dane postaci do wyświetlenia)
    *   `llms: LLMDTO[]` (Lista LLM do znalezienia nazwy modelu)
    *   `onEdit: (id: number) => void` (Callback nawigacji do edycji)
    *   `onDelete: (id: number) => Promise<void>` (Callback inicjujący proces usuwania)
    *   `isDeleting?: boolean` (Opcjonalne, stan ładowania podczas usuwania)

### `DeleteConfirmationDialog` (React Component - `.tsx`)
*   **Opis:** Okno dialogowe (Shadcn `AlertDialog`) do potwierdzenia operacji usunięcia postaci.
*   **Główne elementy:** `AlertDialog` z tytułem, opisem, przyciskami "Potwierdź" i "Anuluj".
*   **Obsługiwane interakcje:** Kliknięcie "Potwierdź", kliknięcie "Anuluj".
*   **Obsługiwana walidacja:** Brak.
*   **Typy:** Brak specyficznych typów.
*   **Propsy:**
    *   `open: boolean` (Kontroluje widoczność dialogu)
    *   `onOpenChange: (open: boolean) => void` (Callback do zmiany stanu `open`)
    *   `onConfirm: () => void` (Callback wywoływany po potwierdzeniu)
    *   `isDeleting?: boolean` (Opcjonalne, do wyłączenia przycisku potwierdzenia podczas usuwania)

## 5. Typy
*   **Istniejące DTO (backendowe):**
    *   `CharacterDTO` (z `ui/src/dailogi-api/model/characterDTO.ts`): Dane postaci.
    *   `CreateCharacterCommand` (z `ui/src/dailogi-api/model/createCharacterCommand.ts`): Typ żądania dla tworzenia postaci.
    *   `UpdateCharacterCommand` (z `ui/src/dailogi-api/model/updateCharacterCommand.ts`): Typ żądania dla aktualizacji postaci.
    *   `CharacterAvatarResponseDTO` (z `ui/src/dailogi-api/model/characterAvatarResponseDTO.ts`): Typ odpowiedzi po wgraniu awatara.
    *   `LLMDTO` (z `ui/src/dailogi-api/model/l L M D T O.ts` - poprawić nazwę pliku, jeśli taka jest): Dane modelu LLM.
    *   `ErrorResponseDTO` (z `ui/src/dailogi-api/model/errorResponseDTO.ts`): Standardowy format odpowiedzi błędu API.
*   **Nowe typy (frontendowe/ViewModel):**
    *   `CharacterFormViewModel` (interfejs lub typ w `CharacterForm.tsx`):
        ```typescript
        interface CharacterFormViewModel {
          name: string;
          short_description: string;
          description: string;
          avatar?: File | null; // File dla nowego, null dla usunięcia, undefined/pominięty jeśli bez zmian
          default_llm_id?: string; // Używamy string dla <select>, konwersja na number przed API
        }
        ```
    *   `LLMOption` (typ dla opcji w `Select`):
        ```typescript
        interface LLMOption {
          value: string; // ID modelu jako string
          label: string; // Nazwa modelu do wyświetlenia
        }
        ```

## 6. Zarządzanie stanem
*   **Formularz (`CharacterForm`):** Stan pól formularza, walidacja i status submisji będą zarządzane przez `react-hook-form`.
*   **Stan asynchroniczny (API):** Zaleca się użycie biblioteki do zarządzania stanem serwera, np. **TanStack Query (React Query)**. Utworzone zostaną hooki:
    *   `useLLMs()`: Do pobierania i cachowania listy LLM.
    *   `useCharacter(characterId: number)`: Do pobierania i cachowania danych pojedynczej postaci.
    *   `useCharacterMutations()`: Do obsługi mutacji (create, update, delete, upload/delete avatar) wraz z zarządzaniem stanami ładowania, błędów i unieważnianiem cache'u.
*   **Stan UI:** Proste stany UI (np. widoczność dialogu potwierdzenia) będą zarządzane za pomocą hooka `useState` w odpowiednich komponentach (`CharacterDetailsWrapper`, `CharacterFormWrapper`).
*   **Pobieranie danych w Astro:** Dane początkowe (LLM, dane postaci dla edycji/podglądu) mogą być pobrane po stronie serwera w komponentach stron Astro (`.astro`) i przekazane jako propsy do komponentów React (`client:*`). Mutacje i odświeżanie danych będą obsługiwane po stronie klienta przez React Query.

## 7. Integracja API
Integracja będzie realizowana za pomocą dedykowanego klienta API (np. instancja Axios lub wrapper wokół `fetch` w `src/lib/apiClient.ts`), który będzie automatycznie dołączał token autoryzacyjny (Bearer Token) pobrany z kontekstu sesji (np. z `Astro.locals` lub ciasteczka).

*   **Tworzenie:**
    *   Submit `CharacterForm` wywołuje `POST /api/characters` z ciałem typu `CreateCharacterCommand`.
    *   Jeśli formularz zawierał awatar, po pomyślnym utworzeniu postaci (otrzymaniu ID), wywoływane jest `POST /api/characters/{id}/avatar` z `FormData` zawierającym plik.
    *   **Typy:** Żądanie: `CreateCharacterCommand`, Odpowiedź: `CharacterDTO`. Żądanie Avatar: `FormData`, Odpowiedź Avatar: `CharacterAvatarResponseDTO`.
*   **Podgląd:**
    *   Ładowanie widoku `/characters/[id]` wywołuje `GET /api/characters/{id}`.
    *   Wyświetlanie awatara używa `GET /api/characters/{id}/avatar` jako `src` dla `<img>` lub komponentu `Avatar`.
    *   **Typy:** Odpowiedź: `CharacterDTO`. Odpowiedź Avatar: `image/*`.
*   **Edycja:**
    *   Ładowanie widoku `/characters/[id]/edit` wywołuje `GET /api/characters/{id}` (do wypełnienia formularza) i `GET /api/llms`.
    *   Submit `CharacterForm` wywołuje `PUT /api/characters/{id}` z ciałem typu `UpdateCharacterCommand`.
    *   Jeśli awatar został dodany/zmieniony, wywoływane jest `POST /api/characters/{id}/avatar`.
    *   Jeśli awatar został usunięty, wywoływane jest `DELETE /api/characters/{id}/avatar`.
    *   **Typy:** Żądanie: `UpdateCharacterCommand`, Odpowiedź: `CharacterDTO`. Żądanie/Odpowiedź Avatar: jak przy tworzeniu.
*   **Usuwanie:**
    *   Potwierdzenie w `DeleteConfirmationDialog` wywołuje `DELETE /api/characters/{id}`.
    *   **Typy:** Odpowiedź: `204 No Content`.
*   **Pobieranie LLM:**
    *   Ładowanie widoków Create/Edit/Details wywołuje `GET /api/llms`.
    *   **Typy:** Odpowiedź: `LLMDTO[]` (API zwraca pojedynczy `LLMDTO` wg schematu, backend musi zwracać listę `LLMDTO[]` - **sprawdzić/poprawić API lub jego dokumentację! Zakładam, że API zwróci listę.**).

## 8. Interakcje użytkownika
*   **Wprowadzanie danych w formularzu:** Aktualizacja stanu `react-hook-form`, walidacja w czasie rzeczywistym (on change/blur).
*   **Wybór awatara:** Otwarcie okna wyboru pliku, walidacja pliku po wyborze, wyświetlenie podglądu lub błędu.
*   **Usunięcie awatara (edycja):** Kliknięcie "Usuń awatar", wyczyszczenie podglądu, oznaczenie awatara do usunięcia przy zapisie.
*   **Wysłanie formularza (Zapisz / Zapisz zmiany):** Walidacja całego formularza. Jeśli poprawny, wywołanie API (create/update + avatar). Wyświetlenie stanu ładowania. Po sukcesie: nawigacja do podglądu/listy, wyświetlenie powiadomienia (np. Toast). Po błędzie: wyświetlenie komunikatu błędu (ogólnego lub specyficznego dla pola).
*   **Anulowanie formularza:** Nawigacja do poprzedniej strony (podgląd lub lista postaci).
*   **Kliknięcie "Edytuj" (podgląd):** Nawigacja do `/characters/[id]/edit`.
*   **Kliknięcie "Usuń" (podgląd):** Otwarcie `DeleteConfirmationDialog`.
*   **Potwierdzenie usunięcia:** Wywołanie API DELETE. Wyświetlenie stanu ładowania. Po sukcesie: nawigacja do listy postaci, wyświetlenie powiadomienia. Po błędzie: wyświetlenie komunikatu błędu.
*   **Anulowanie usunięcia:** Zamknięcie dialogu.

## 9. Warunki i walidacja
*   **Walidacja pól formularza:** Zgodnie z sekcją 4 (`CharacterForm`), implementowana przy użyciu `react-hook-form` i reguł zgodnych z ograniczeniami API (`required`, `minLength`, `maxLength`). Komunikaty o błędach wyświetlane przy polach. Przycisk zapisu jest nieaktywny, jeśli formularz jest niepoprawny.
*   **Walidacja awatara:** Zgodnie z sekcją 4 (`AvatarUploader`), implementowana w komponencie `AvatarUploader`. Obejmuje typ MIME, rozmiar pliku oraz wymiary obrazu (asynchronicznie). Komunikaty o błędach wyświetlane w obrębie komponentu. Błędny plik nie jest przekazywany do `CharacterForm`.
*   **Limit postaci (422):** Po otrzymaniu błędu 422 z API przy próbie stworzenia postaci, wyświetlany jest `AlertDialog` informujący o osiągnięciu limitu.
*   **Konflikt nazwy (409):** Po otrzymaniu błędu 409 (Create/Update), błąd jest przypisywany do pola "Nazwa" w `react-hook-form` i wyświetlany przy tym polu.
*   **Postać używana (409):** Po otrzymaniu błędu 409 przy próbie usunięcia, wyświetlany jest komunikat w `DeleteConfirmationDialog` lub jako Toast, informujący, że postać nie może być usunięta, ponieważ jest używana.
*   **Ochrona routingu:** Middleware Astro weryfikuje zalogowanie użytkownika przed dostępem do ścieżek `/characters/new`, `/characters/[id]`, `/characters/[id]/edit`. Niezalogowani użytkownicy są przekierowywani na stronę logowania.
*   **Autoryzacja (403):** Jeśli API zwróci 403 przy próbie edycji/usunięcia, użytkownikowi wyświetlany jest komunikat o braku uprawnień. W idealnym scenariuszu, przyciski edycji/usunięcia byłyby nieaktywne dla postaci globalnych lub nie należących do użytkownika (wymagałoby to informacji o właścicielu w `CharacterDTO`).

## 10. Obsługa błędów
*   **Błędy walidacji (klient):** Obsługiwane przez `react-hook-form` i `AvatarUploader`, komunikaty wyświetlane przy polach/komponencie.
*   **Błędy API (4xx, 5xx):**
    *   Globalny interceptor w kliencie API może obsługiwać błędy `401 Unauthorized` (przekierowanie do logowania).
    *   Komponenty (`CharacterFormWrapper`, `CharacterDetailsWrapper`) lub hooki mutacji (`useCharacterMutations`) będą łapać pozostałe błędy API.
    *   Błędy będą mapowane na komunikaty przyjazne dla użytkownika (np. za pomocą dedykowanej funkcji `mapApiErrorToMessage`).
    *   Błędy specyficzne dla pól (np. 409 dla nazwy) będą wyświetlane przy odpowiednich polach formularza.
    *   Błędy ogólne (5xx, błędy sieciowe, 403, 404) będą wyświetlane jako komunikaty ogólne (np. Shadcn Toast lub dedykowany obszar błędu w komponencie).
    *   Błąd 422 (limit postaci) będzie obsługiwany przez dedykowany `AlertDialog`.
    *   Błąd 409 (postać w użyciu) przy usuwaniu będzie wyświetlany jako Toast lub w dialogu.
*   **Błędy ładowania danych (Podgląd/Edycja):** Stany `isLoading` i `error` zarządzane przez React Query (lub `useState` przy prostym `fetch`). Komponenty `CharacterDetailsWrapper`/`CharacterFormWrapper` będą wyświetlać wskaźnik ładowania lub komunikat o błędzie zamiast treści, jeśli dane nie zostaną pobrane poprawnie.

## 11. Kroki implementacji
1.  **Przygotowanie środowiska:** Upewnić się, że projekt Astro jest skonfigurowany do używania React i TypeScript. Zainstalować `react-hook-form` i ewentualnie `TanStack Query (React Query)`.
2.  **Struktura plików:** Utworzyć pliki dla stron Astro (`src/pages/characters/new.astro`, `src/pages/characters/[id].astro`, `src/pages/characters/[id]/edit.astro`). Utworzyć pliki dla komponentów React w `src/components/` (np. `CharacterFormWrapper.tsx`, `CharacterForm.tsx`, `AvatarUploader.tsx`, `CharacterDetailsWrapper.tsx`, `CharacterDetails.tsx`, `DeleteConfirmationDialog.tsx`).
3.  **Middleware:** Zaimplementować lub zweryfikować middleware Astro (`src/middleware/index.ts`) chroniący ścieżki `/characters/*`.
4.  **Klient API:** Skonfigurować klienta API (`src/lib/apiClient.ts`) do obsługi autoryzacji i base URL.
5.  **Typy:** Zweryfikować i ewentualnie poprawić wygenerowane typy DTO. Zdefiniować typy ViewModel (`CharacterFormViewModel`, `LLMOption`).
6.  **Komponent `AvatarUploader`:** Zaimplementować logikę wyboru pliku, walidacji (typ, rozmiar, wymiary - asynchronicznie!), podglądu i usuwania. Użyć komponentów Shadcn.
7.  **Komponent `CharacterForm`:** Zaimplementować formularz używając `react-hook-form` i komponentów Shadcn. Zintegrować `AvatarUploader`. Dodać logikę walidacji zgodną z API. Obsłużyć tryb tworzenia i edycji (wypełnianie danych początkowych). Zaimplementować logikę `onSubmit` wywołującą odpowiednie API (create/update + avatar). Dodać obsługę błędu 422 (`AlertDialog`).
8.  **Komponent `DeleteConfirmationDialog`:** Zaimplementować dialog używając `AlertDialog` z Shadcn.
9.  **Komponent `CharacterDetails`:** Zaimplementować wyświetlanie danych postaci (przyjmując `character` i `llms` jako propsy). Dodać przyciski "Edytuj" i "Usuń". Zintegrować `DeleteConfirmationDialog`.
10. **Komponenty Wrapper (`CharacterFormWrapper`, `CharacterDetailsWrapper`):** Zaimplementować pobieranie danych (LLM, dane postaci dla edycji/podglądu) - zalecane użycie React Query (`useLLMs`, `useCharacter`). Obsłużyć stany ładowania i błędów. Przekazać dane i callbacki do komponentów dzieci (`CharacterForm`, `CharacterDetails`). Zaimplementować logikę mutacji (create, update, delete) - zalecane użycie React Query (`useCharacterMutations`). Obsłużyć nawigację po sukcesie/anulowaniu.
11. **Strony Astro:** Utworzyć strony Astro, osadzić w nich odpowiednie komponenty Wrapper React (`client:load` lub `client:visible`). Przekazać `characterId` z `Astro.params` do wrapperów dla podglądu/edycji.
12. **Styling:** Użyć Tailwind i klas Shadcn do stylizacji komponentów zgodnie z ogólnym wyglądem aplikacji.
13. **Testowanie:** Przetestować wszystkie przepływy użytkownika (tworzenie, edycja, podgląd, usuwanie, obsługa awatarów), walidację oraz obsługę błędów API.
