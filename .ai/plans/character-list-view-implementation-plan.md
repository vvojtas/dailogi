# Plan implementacji widoku: Lista Postaci (Zunifikowany)

## 1. Przegląd
Widok "Lista Postaci" ma na celu wyświetlenie paginowanej listy postaci AI dostępnych w aplikacji. Widok jest zunifikowany dla użytkowników zalogowanych i gości:
- **Goście:** Widzą tylko listę postaci globalnych w trybie tylko do odczytu.
- **Zalogowani użytkownicy:** Widzą połączoną listę swoich postaci oraz postaci globalnych. Mogą zarządzać swoimi postaciami (edycja, usuwanie) oraz tworzyć nowe.
Widok wykorzystuje paginację do przeglądania dużej liczby postaci.

## 2. Routing widoku
Widok powinien być dostępny pod jedną ścieżką `/characters` dla wszystkich użytkowników. Dostępność funkcji zarządzania postaciami (tworzenie, edycja, usuwanie) będzie zależna od stanu autentykacji użytkownika, który powinien być sprawdzany (np. w middleware Astro lub bezpośrednio w komponencie React).

## 3. Struktura komponentów
Hierarchia komponentów dla zunifikowanego widoku:

```
src/pages/characters.astro
└── src/components/characters/CharacterListPage.tsx (React, client:load)
    ├── (jeśli zalogowany) src/components/ui/Button.tsx (Shadcn, "Stwórz nową postać")
    ├── src/components/ui/LoadingSpinner.tsx (React, warunkowo)
    ├── src/components/ui/ErrorMessage.tsx (React, warunkowo)
    ├── (jeśli brak postaci) Komunikat (zależny od stanu zalogowania, React, warunkowo)
    ├── (jeśli są postacie) div (kontener listy, np. grid)
    │   └── src/components/characters/CharacterCard.tsx (React, mapowany dla każdej postaci)
    │       ├── src/components/ui/Badge.tsx (Shadcn, "Twoja"/"Globalna", warunkowo)
    │       ├── src/components/characters/CharacterAvatar.tsx (React)
    │       │   └── src/components/ui/Avatar.tsx (Shadcn)
    │       ├── src/components/ui/Card.tsx (Shadcn)
    │       │   ├── CardHeader, CardTitle (Nazwa postaci)
    │       │   ├── CardContent (Krótki opis)
    │       │   └── CardFooter (kontener na przyciski)
    │       │       ├── src/components/ui/Button.tsx (Shadcn, "Zobacz szczegóły")
    │       │       ├── (jeśli właściciel) src/components/ui/Button.tsx (Shadcn, "Edytuj")
    │       │       └── (jeśli właściciel) src/components/ui/Button.tsx (Shadcn, "Usuń")
    ├── src/components/ui/PaginationControl.tsx (React, warunkowo, jeśli totalPages > 1)
    │   └── src/components/ui/Pagination.tsx (Shadcn)
    └── (jeśli zalogowany) src/components/ui/AlertDialog.tsx (Shadcn, potwierdzenie usunięcia, warunkowo)
```

## 4. Szczegóły komponentów

### `characters.astro`
- **Opis komponentu:** Główna strona Astro dla ścieżki `/characters`. Renderuje layout i osadza komponent React `CharacterListPage`. Może (opcjonalnie) pobrać stan zalogowania z `Astro.locals` (jeśli ustawiony przez middleware) i przekazać go jako props do komponentu React.
- **Główne elementy:** Layout aplikacji, `<CharacterListPage client:load isLoggedIn={Astro.locals.isLoggedIn ?? false} />`.
- **Obsługiwane interakcje:** Brak.
- **Obsługiwana walidacja:** Brak.
- **Typy:** Brak specyficznych.
- **Propsy:** Brak (lub opcjonalnie `isLoggedIn: boolean`).

### `CharacterListPage.tsx` (zastępuje `GlobalCharacterList.tsx`)
- **Opis komponentu:** Główny komponent React strony. Odpowiada za pobieranie danych (własnych i/lub globalnych postaci), zarządzanie stanem (ładowanie, błędy, paginacja, stan dialogu potwierdzenia), renderowanie listy (`CharacterCard`) lub komunikatów, oraz obsługę akcji użytkownika (nawigacja do tworzenia, edycji, inicjowanie usuwania).
- **Główne elementy:** `Button` ("Stwórz nową"), `LoadingSpinner`, `ErrorMessage`, `CharacterCard` (mapowany), `PaginationControl`, `AlertDialog` (potwierdzenie usunięcia), warunkowe komunikaty.
- **Obsługiwane interakcje:** Zmiana strony w `PaginationControl`, kliknięcie "Stwórz nową", "Edytuj", "Usuń" na karcie postaci, potwierdzenie/anulowanie w `AlertDialog`.
- **Obsługiwana walidacja:** Walidacja parametrów `page` i `size` z URL. Sprawdzenie uprawnień przed próbą usunięcia (choć główna walidacja jest w API).
- **Typy:** `CharacterDTO`, `CharacterListDTO`, `GetCharactersParams`, wewnętrzny stan (np. `characters`, `isLoading`, `error`, `currentPage`, `totalPages`, `pageSize`, `isLoggedIn`, `characterToDelete`, `isDeleteDialogOpen`).
- **Propsy:**
    - `isLoggedIn: boolean`: Informuje, czy użytkownik jest zalogowany (może być też pobierany z kontekstu/store).
- **Logika:** Określa `isOwner` dla każdej karty (`!character.is_global && isLoggedIn`). Obsługuje cykl życia dialogu potwierdzenia usunięcia. Wywołuje odpowiednie funkcje API (`getCharacters`, `deleteCharacter`).

### `CharacterCard.tsx`
- **Opis komponentu:** Wyświetla informacje o postaci. Teraz zawiera dodatkowe elementy i logikę dla zalogowanych użytkowników i ich postaci.
- **Główne elementy:** Shadcn `Card`, `Badge` ("Twoja"/"Globalna"), `CharacterAvatar`, `CardHeader`, `CardTitle`, `CardContent`, `CardFooter`, `Button` ("Zobacz szczegóły"), warunkowe `Button` ("Edytuj", "Usuń").
- **Obsługiwane interakcje:** Kliknięcie "Zobacz szczegóły", "Edytuj", "Usuń".
- **Obsługiwana walidacja:** Brak.
- **Typy:** Wymaga `CharacterDTO`.
- **Propsy:**
    - `character: CharacterDTO`: Obiekt DTO postaci.
    - `isOwner: boolean`: Czy zalogowany użytkownik jest właścicielem tej postaci.
    - `onEdit?: (characterId: number) => void`: Funkcja zwrotna do obsługi edycji.
    - `onDelete?: (character: CharacterDTO) => void`: Funkcja zwrotna do zainicjowania usuwania (przekazuje cały obiekt dla dialogu).
    - `onViewDetails?: (characterId: number) => void`: Funkcja zwrotna do obsługi nawigacji do szczegółów.

### `CharacterAvatar.tsx`
- **Opis komponentu:** Komponent React do wyświetlania awatara postaci przy użyciu Shadcn `Avatar`. Wyświetla placeholder (np. inicjały), jeśli postać nie ma awatara.
- **Główne elementy:** Shadcn `Avatar`, `AvatarImage`, `AvatarFallback`.
- **Obsługiwane interakcje:** Brak.
- **Obsługiwana walidacja:** Brak.
- **Typy:** Wymaga `has_avatar`, `avatar_url`, `name`.
- **Propsy:**
    - `hasAvatar: boolean`: Czy postać posiada awatar.
    - `avatarUrl?: string`: URL do awatara (jeśli `hasAvatar` jest true).
    - `characterName: string`: Nazwa postaci (używana do fallbacku).
    - `className?: string`: Opcjonalne klasy CSS.

### `PaginationControl.tsx`
- **Opis komponentu:** Komponent React renderujący kontrolki paginacji (Shadcn `Pagination`). Wyświetla numery stron, przyciski "poprzednia"/"następna" i obsługuje nawigację między stronami.
- **Główne elementy:** Shadcn `Pagination`, `PaginationContent`, `PaginationItem`, `PaginationLink`, `PaginationPrevious`, `PaginationNext`, `PaginationEllipsis`.
- **Obsługiwane interakcje:** Kliknięcie numeru strony, przycisków "poprzednia" lub "następna".
- **Obsługiwana walidacja:** Dezaktywacja przycisków "poprzednia"/"następna" na pierwszej/ostatniej stronie.
- **Typy:** Wymaga `currentPage`, `totalPages`.
- **Propsy:**
    - `currentPage: number`: Aktualny numer strony (0-indeksowany).
    - `totalPages: number`: Całkowita liczba stron.
    *   `onPageChange: (newPage: number) => void`: Funkcja zwrotna wywoływana przy zmianie strony, przekazująca nowy numer strony (0-indeksowany).
    *   `className?: string`: Opcjonalne klasy CSS.

### `LoadingSpinner.tsx`
- **Opis komponentu:** Prosty komponent React wyświetlający wskaźnik ładowania.
- **Główne elementy:** Dowolny element wizualny wskazujący ładowanie (np. animowany SVG).
- **Obsługiwane interakcje:** Brak.
- **Obsługiwana walidacja:** Brak.
- **Typy:** Brak.
- **Propsy:** Brak.

### `ErrorMessage.tsx`
- **Opis komponentu:** Prosty komponent React wyświetlający komunikat błędu.
- **Główne elementy:** Element tekstowy (np. `<p>`, `<div>`) z odpowiednim stylem wskazującym błąd.
- **Obsługiwane interakcje:** Brak.
- **Obsługiwana walidacja:** Brak.
- **Typy:** Wymaga `message`.
- **Propsy:**
    - `message: string`: Treść komunikatu błędu do wyświetlenia.
    *   `className?: string`: Opcjonalne klasy CSS.

### `AlertDialog` (Komponent z Shadcn/ui)
- **Opis komponentu:** Używany do wyświetlenia modalnego okna dialogowego z prośbą o potwierdzenie akcji usunięcia postaci.
- **Główne elementy:** `AlertDialogTrigger` (może być niewidoczny, sterowany programowo), `AlertDialogContent`, `AlertDialogHeader`, `AlertDialogTitle` ("Czy na pewno?"), `AlertDialogDescription` ("Tej akcji nie można cofnąć. Spowoduje to trwałe usunięcie postaci '[nazwa postaci]'."), `AlertDialogFooter`, `AlertDialogCancel`, `AlertDialogAction` ("Usuń").
- **Obsługiwane interakcje:** Kliknięcie "Anuluj", kliknięcie "Usuń".
- **Obsługiwana walidacja:** Brak.
- **Typy:** Sterowany stanami `isOpen` i danymi postaci do usunięcia w `CharacterListPage`.
- **Propsy:** Standardowe propsy Shadcn `AlertDialog`.

## 5. Typy
- **DTO i Parametry API:** `CharacterDTO`, `CharacterListDTO`, `GetCharactersParams` - bez zmian. Typy te pochodzą z `ui/src/dailogi-api/model/`.
- **Typy wewnętrzne/ViewModel:** Nie ma potrzeby tworzenia nowych złożonych typów. Komponenty będą używać prostych typów (np. `boolean` dla `isLoggedIn`, `isOwner`) oraz stanów do zarządzania dialogiem (`characterToDelete: CharacterDTO | null`).

## 6. Zarządzanie stanem
Zarządzanie stanem realizowane głównie w `CharacterListPage.tsx` za pomocą hooków React (`useState`, `useEffect`) lub opcjonalnie globalnego store'a/kontekstu dla stanu autentykacji.

- **Stany w `CharacterListPage`:**
    - `characters: CharacterDTO[]`
    - `isLoading: boolean`
    - `error: string | null`
    - `currentPage: number`
    - `totalPages: number`
    - `pageSize: number`
    - `isLoggedIn: boolean` (z propsa lub kontekstu)
    - `characterToDelete: CharacterDTO | null` (dla dialogu potwierdzenia)
    - `isDeleteDialogOpen: boolean` (sterowanie widocznością dialogu)
- **Logika:**
    - `useEffect` do pobierania danych (`getCharacters`) przy zmianie `currentPage`, `pageSize`, `isLoggedIn`.
    - Funkcje obsługi akcji:
        - `handleNavigateToCreate()`: Przekierowuje do strony tworzenia postaci.
        - `handleNavigateToEdit(characterId: number)`: Przekierowuje do strony edycji postaci.
        - `handleDeleteClick(character: CharacterDTO)`: Ustawia `characterToDelete` i `isDeleteDialogOpen` na `true`.
        - `handleConfirmDelete()`: Wywołuje `deleteCharacter(characterToDelete.id)`, zamyka dialog, odświeża listę (lub usuwa element ze stanu). Obsługuje błędy usuwania.
        - `handleCancelDelete()`: Resetuje `characterToDelete`, ustawia `isDeleteDialogOpen` na `false`.
    - Logika do wyliczenia `isOwner` dla `CharacterCard`: `!character.is_global && isLoggedIn`.

## 7. Integracja API
- **`GET /api/characters`:**
    - Funkcja klienta: `getCharacters`.
    - Używana do pobierania listy postaci. Parametry (`page`, `size`, `include_global: true`). Backend sam decyduje, co zwrócić na podstawie JWT (lub jego braku).
    - Odpowiedź: `AxiosResponse<CharacterListDTO>`.
- **`DELETE /api/characters/{id}`:**
    - Funkcja klienta: `deleteCharacter`.
    - Używana do usuwania postaci należącej do użytkownika. Wymaga `id` postaci.
    - Odpowiedź: `AxiosResponse<string>` (sukces) lub błąd. Wymaga autentykacji (obsługiwane przez `axios interceptor` lub przekazanie tokena).
- **Przepływ usuwania:**
    1. Użytkownik klika "Usuń" na karcie swojej postaci.
    2. `handleDeleteClick` w `CharacterListPage` ustawia stan do otwarcia `AlertDialog` z danymi postaci.
    3. Użytkownik klika "Usuń" w `AlertDialog`.
    4. `handleConfirmDelete` wywołuje `deleteCharacter(id)`.
    5. Po sukcesie: zamyka dialog, odświeża listę postaci (ponowne wywołanie `getCharacters` lub optymistyczne usunięcie ze stanu `characters`).
    6. W przypadku błędu: wyświetla komunikat błędu (np. za pomocą `toast` z Shadcn/ui), zamyka dialog.

## 8. Interakcje użytkownika
- **Wejście na stronę `/characters`:**
    - Gość: Widzi listę globalnych postaci, paginację.
    - Zalogowany: Widzi przycisk "Stwórz nową", listę swoich i globalnych postaci (oznaczonych), paginację, przyciski "Edytuj"/"Usuń" na swoich kartach.
- **Kliknięcie "Stwórz nową" (zalogowany):** Przekierowanie na stronę tworzenia postaci.
- **Kliknięcie "Zobacz szczegóły":** Przekierowanie na stronę szczegółów postaci (`/characters/{id}`).
- **Kliknięcie "Edytuj" (zalogowany, na własnej postaci):** Przekierowanie na stronę edycji postaci (`/characters/{id}/edit`).
- **Kliknięcie "Usuń" (zalogowany, na własnej postaci):** Otwarcie `AlertDialog` z pytaniem o potwierdzenie.
    - Kliknięcie "Anuluj" w dialogu: Zamknięcie dialogu.
    - Kliknięcie "Usuń" w dialogu: Wywołanie API usuwania, zamknięcie dialogu, odświeżenie listy.
- **Zmiana strony (paginacja):** Aktualizacja listy postaci.
- **Komunikaty:** Wyświetlanie odpowiednich komunikatów o ładowaniu, błędach lub braku postaci (zależnie od stanu zalogowania).

## 9. Warunki i walidacja
- **Walidacja parametrów URL (`page`, `size`):** Jak w poprzednim planie.
- **Dostępność akcji:** Przyciski "Stwórz nową", "Edytuj", "Usuń" oraz dialog potwierdzenia są dostępne/renderowane tylko dla zalogowanych użytkowników. "Edytuj" i "Usuń" dodatkowo tylko dla postaci, których `isOwner` jest `true`.
- **Walidacja usuwania:** Przed wywołaniem API `deleteCharacter`, komponent może upewnić się, że `characterToDelete` nie jest `null`. Główna walidacja (czy użytkownik jest właścicielem, czy postać nie jest używana - jeśli taka reguła istnieje) odbywa się po stronie backendu.

## 10. Obsługa błędów
- **Błędy `getCharacters`:** Jak w poprzednim planie (błędy sieciowe, serwera, 4xx - wyświetlanie `ErrorMessage`).
- **Błędy `deleteCharacter`:**
    - **401 Unauthorized / 403 Forbidden:** Użytkownik nie jest zalogowany lub nie jest właścicielem postaci. Wyświetlić komunikat błędu (np. toast).
    - **404 Not Found:** Postać o danym ID nie istnieje. Wyświetlić komunikat błędu, odświeżyć listę.
    - **409 Conflict (jeśli zaimplementowane):** Postać jest używana np. w zapisanych dialogach i nie może być usunięta. Wyświetlić informacyjny komunikat błędu.
    - **Inne błędy (5xx, sieciowe):** Wyświetlić ogólny komunikat błędu.
- **Brak postaci:** Obsługa jak w poprzednim planie, ale z rozróżnieniem komunikatu dla gościa i zalogowanego użytkownika (`!isLoading && !error && characters.length === 0`).
    - Gość: "Brak postaci globalnych do wyświetlenia."
    - Zalogowany: "Nie masz jeszcze żadnych postaci. Stwórz pierwszą!" (z linkiem/przyciskiem do tworzenia).

## 11. Kroki implementacji
1.  **Aktualizacja strony Astro (`src/pages/characters.astro`):** Zmień nazwę renderowanego komponentu na `CharacterListPage` i (opcjonalnie) przekaż props `isLoggedIn`.
2.  **Refaktoryzacja/Zmiana nazwy komponentu React:** Zmień nazwę `GlobalCharacterList.tsx` na `CharacterListPage.tsx`.
3.  **Implementacja logiki stanu autentykacji:** W `CharacterListPage` dodaj props `isLoggedIn` lub zintegruj z globalnym stanem/kontekstem autentykacji.
4.  **Dodanie przycisku "Stwórz nową":** W `CharacterListPage` dodaj `Button` widoczny tylko dla `isLoggedIn`, obsługujący nawigację do tworzenia postaci.
5.  **Aktualizacja logiki pobierania danych:** Upewnij się, że `getCharacters` jest wywoływane z `include_global: true` zawsze. Dane zostaną odpowiednio przefiltrowane przez backend.
6.  **Adaptacja `CharacterCard.tsx`:**
    - Dodaj propsy `isOwner`, `onEdit`, `onDelete`, `onViewDetails`.
    - Dodaj warunkowe renderowanie `Badge` ("Twoja"/"Globalna") na podstawie `character.is_global`.
    - Dodaj warunkowe renderowanie przycisków "Edytuj" i "Usuń" w `CardFooter` na podstawie `isOwner`.
    - Podłącz funkcje zwrotne `onEdit`, `onDelete`, `onViewDetails` do odpowiednich przycisków.
7.  **Przekazanie propsów do `CharacterCard`:** W `CharacterListPage`, podczas mapowania `characters`, wylicz `isOwner` i przekaż odpowiednie propsy (w tym funkcje obsługi) do `CharacterCard`.
8.  **Implementacja funkcji obsługi akcji:** W `CharacterListPage` zaimplementuj `handleNavigateToCreate`, `handleNavigateToEdit`, `handleDeleteClick`, `handleConfirmDelete`, `handleCancelDelete`.
9.  **Implementacja `AlertDialog`:** W `CharacterListPage` dodaj komponent `AlertDialog` z Shadcn/ui. Steruj jego widocznością za pomocą stanów `isDeleteDialogOpen` i `characterToDelete`. Podłącz akcje "Anuluj" i "Usuń" do `handleCancelDelete` i `handleConfirmDelete`.
10. **Integracja API `deleteCharacter`:** W `handleConfirmDelete` wywołaj `deleteCharacter` z ID postaci ze stanu `characterToDelete`.
11. **Obsługa odświeżania listy po usunięciu:** Zdecyduj o strategii - ponowne pobranie danych (`getCharacters`) lub optymistyczne usunięcie elementu ze stanu `characters`.
12. **Aktualizacja komunikatów:** Zmodyfikuj logikę renderowania komunikatów o braku danych, aby wyświetlać różne teksty w zależności od `isLoggedIn`.
13. **Obsługa błędów usuwania:** Dodaj obsługę błędów w `catch` dla wywołania `deleteCharacter`, wyświetlając np. toasty.
14. **Styling:** Dostosuj style (Tailwind), aby uwzględnić nowe elementy (badge, przyciski akcji). Upewnij się, że układ kart jest responsywny (np. `grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4`).
15. **Testowanie:** Gruntownie przetestuj oba scenariusze (zalogowany/niezalogowany), wszystkie interakcje (tworzenie, edycja, usuwanie, paginacja), obsługę błędów i przypadki brzegowe (brak postaci). 