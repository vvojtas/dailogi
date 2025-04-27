<conversation_summary>
<decisions>
1.  **Dialog Generation:** Synchronous (`POST /api/dialogues/generate` returns full JSON response), no streaming/WebSockets for MVP.
2.  **Character Selection (Scene):** Use 2-3 `Select` dropdown components. Options show avatar, name, and have conditional styling for global characters. Load all characters for dropdowns.
3.  **Character Display (List):** Use `Card` components on the main `/characters` page.
4.  **Character CRUD:** Use separate pages for viewing (`/characters/[id]`), creating (`/characters/new`), and editing (`/characters/[id]/edit`).
5.  **Character Viewing (Public):** Allow viewing global characters without login (requires public access to `GET /api/characters/{id}` for global characters).
6.  **Resource Limits:** Handle 422 error from API (`POST /api/characters`, `POST /api/dialogues`) by showing a pop-up (`AlertDialog`). No proactive limit display.
7.  **API Error Handling:** Display generic error messages to the user via `Toast` components.
8.  **Special User Feature:** No specific UI treatment required; logic handled by the backend.
9.  **Responsiveness:** Desktop-first approach for MVP.
10. **Accessibility:** No specific WCAG requirements mandated for MVP.
11. **Authentication (JWT):** Store JWT in `localStorage`. API does not support token refresh.
12. **Avatars:** `CharacterDTO.avatar_url` and the response from `POST /api/characters/{id}/avatar` contain the avatar as a Data URL. Use a standard placeholder when no avatar exists.
13. **Scene Description:** Required field during scene setup.
14. **LLM Selection:** Pre-select the default LLM if set for a character, but allow the user to override it. Selection is mandatory if no default is set.
15. **Dialog Saving:** A "Save" button appears after successful dialogue generation. Clicking it triggers `POST /api/dialogues` with the full dialogue object (messages, config, status, scene description) plus the user-provided `name`. No dialogue ID is sent in this request.
16. **Default Dialog Name Suggestion:** "Rozmowa |Postać1|Postać 2|Postać3| - Data".
</decisions>
<matched_recommendations>
1.  **Routing/Layout:** Utilize Astro for routing and layouts (`AuthLayout`, `AppLayout`), creating separate pages for character CRUD, viewing, scene setup, dialogue history, etc., protected by middleware.
2.  **UI Components:** Employ React for interactive islands and Shadcn/ui components (`Card` for character list, `Select` for scene character choice, `Avatar`, `Input`, `Button`, `Textarea`, `Dialog`, `Toast`, `AlertDialog`, `Pagination`, etc.).
3.  **State Management:** Use a global store (e.g., Zustand) for user data, authentication status, and fetched LLM list. Store the JWT in `localStorage`. Use local React component state to hold the generated dialogue object before it's saved.
4.  **API Communication:** Implement hooks/helpers for `fetch`, automatically attaching the JWT from `localStorage` to `Authorization` headers. Centralize error handling (map `ErrorResponseDTO` to `Toast` messages, handle 401 by clearing `localStorage` and redirecting to login). Fetch all characters at once for the scene setup `Select` components.
5.  **Validation:** Implement client-side form validation (`react-hook-form` + `zod`) consistent with API requirements, including specific checks for avatar uploads (type, size, dimensions).
6.  **Authentication Flow:** On login success, store JWT in `localStorage`, update auth state. On logout, clear JWT and update state. Implement necessary security considerations (e.g., data sanitization) due to `localStorage` usage.
7.  **Dialogue Generation/Save Flow:** Trigger synchronous `POST /api/dialogues/generate`, show loader, display results upon receiving the full JSON response. Store response in local state. If successful, show save UI (pre-filled name input + save button). On save click, send the full dialogue object from local state plus the user-provided name via `POST /api/dialogues`.
8.  **Avatar Handling:** Use Data URLs directly from `CharacterDTO` and `CharacterAvatarResponseDTO`. Use placeholders (`AvatarFallback`) when `has_avatar` is false. Update UI immediately after avatar upload using the response Data URL.
9.  **Limit Handling:** Handle 422 errors from `POST` requests for characters/dialogues by displaying an `AlertDialog`.
10. **Responsiveness:** Focus styles and layout on desktop views.
11. **API Key Input:** Use `<Input type="password">` for the API key field.
12. **List Pagination:** Implement pagination using API parameters and `Pagination` components for the main character list (`/characters`) and dialogue history (`/dialogues`).
</matched_recommendations>
<ui_architecture_planning_summary>
**a. Główne wymagania dotyczące architektury UI:**
*   Interfejs użytkownika dla aplikacji d-AI-logi MVP, zbudowany przy użyciu Astro 5 i React 19 (dla komponentów interaktywnych) ze stylistyką Tailwind 4 i komponentami Shadcn/ui.
*   Aplikacja jednostronicowa (SPA) z routingiem po stronie klienta obsługiwanym przez Astro.
*   Koncentracja na funkcjonalności MVP: uwierzytelnianie, zarządzanie postaciami (CRUD), konfiguracja sceny, synchroniczne generowanie dialogu (max 50 tur), przeglądanie/usuwanie historii dialogów, zarządzanie kluczem API OpenRouter.
*   Priorytet dla widoku desktopowego.

**b. Kluczowe widoki, ekrany i przepływy użytkownika:**
*   **Uwierzytelnianie:** Strony `/login`, `/register`.
*   **Panel Główny:** `/` (po zalogowaniu, z linkami do głównych sekcji).
*   **Postaci:**
    *   `/characters`: Lista postaci użytkownika i globalnych (wyświetlane jako `Card`, paginowane).
    *   `/characters/new`: Formularz tworzenia nowej postaci (osobna strona).
    *   `/characters/[id]`: Widok podglądu postaci (dostępny publicznie dla globalnych).
    *   `/characters/[id]/edit`: Formularz edycji postaci (osobna strona).
*   **Dialogi:**
    *   `/dialogues`: Lista zapisanych dialogów (historia, paginowana).
    *   `/dialogues/[id]`: Widok podglądu zapisanego dialogu.
    *   `/dialogues/new`: Strona konfiguracji nowej sceny (opis sceny, 2-3 `Select`-y do wyboru postaci, wybór LLM dla każdej postaci).
    *   `/dialogues/generate` (lub podobny URL): Widok wyświetlający wskaźnik ładowania podczas generowania, a następnie wynikowy dialog (wiadomości + status) i opcję zapisu.
*   **Profil/Ustawienia:** `/profile` (lub podobny): Zarządzanie kluczem API OpenRouter.

**c. Strategia integracji z API i zarządzania stanem:**
*   **Zarządzanie Stanem:** Globalny store (np. Zustand) dla danych użytkownika, stanu uwierzytelnienia, listy LLM. JWT przechowywany w `localStorage`. Lokalny stan React dla obiektu wygenerowanego dialogu przed zapisem.
*   **Komunikacja API:** Zcentralizowane funkcje/hooki do obsługi `fetch`. Automatyczne dołączanie tokenu JWT z `localStorage`. Centralna obsługa błędów (401 -> logout, inne błędy -> `Toast`).
*   **Kluczowe Endpoints:** Wykorzystanie endpointów zdefiniowanych w `api-plan.md` i `be-api.json` zgodnie z ustaleniami (np. synchroniczny `POST /api/dialogues/generate`, `POST /api/dialogues` do zapisu pełnego obiektu).

**d. Kwestie dotyczące responsywności, dostępności i bezpieczeństwa:**
*   **Responsywność:** Projekt desktop-first, responsywność nie jest priorytetem dla MVP.
*   **Dostępność:** Brak specyficznych wymagań WCAG dla MVP.
*   **Bezpieczeństwo:** JWT przechowywany w `localStorage` (wymaga uwagi na ryzyko XSS i stosowania środków zaradczych jak sanitacja danych). Klucz API OpenRouter wprowadzany w polu typu `password`.

**e. Wszelkie nierozwiązane kwestie lub obszary wymagające dalszego wyjaśnienia:**
*   Należy zapewnić, że endpoint `GET /api/characters/{id}` faktycznie zezwala na dostęp publiczny do postaci globalnych (`is_global=true`) bez uwierzytelniania, zgodnie z podjętą decyzją.
</ui_architecture_planning_summary>
<unresolved_issues>
1.  Konieczność potwierdzenia/zapewnienia przez backend, że endpoint `GET /api/characters/{id}` umożliwia publiczny, nieuwierzytelniony dostęp do postaci oznaczonych jako globalne (`is_global=true`).
</unresolved_issues>
</conversation_summary> 