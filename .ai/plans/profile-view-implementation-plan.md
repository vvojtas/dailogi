# Plan implementacji widoku Profil Użytkownika

## 1. Przegląd
Widok Profil Użytkownika pozwala zalogowanemu użytkownikowi zarządzać swoim kluczem API OpenRouter: sprawdzić, czy klucz jest zapisany, wprowadzić/aktualizować klucz oraz usunąć go. Klucz jest maskowany, a operacje potwierdzane wizualnie (badge, toasty).

## 2. Routing widoku
Ścieżka: `/profile`

- W Astro: plik `src/pages/profile.astro` lub `src/pages/profile.tsx` z odpowiednią adnotacją routingu.
- Middleware: chronić ścieżkę przed dostępem niezalogowanych (np. w `ui/src/middleware/index.ts`).

## 3. Struktura komponentów

ProfilePage
├─ ApiKeyStatus
├─ ApiKeyForm
│ ├─ ShadcnInput (type="password")
│ ├─ ShadcnButton ("Zapisz klucz")
│ └─ ShadcnButton ("Usuń klucz")
└─ ToastContainer (Shadcn/ui ToastProvider)

## 4. Szczegóły komponentów

### ProfilePage
- Opis: kontener całego widoku, zarządza hookiem `useApiKey`.
- Elementy:
  - `<ApiKeyStatus hasApiKey={hasApiKey} />`
  - `<ApiKeyForm apiKey={apiKey} hasApiKey={hasApiKey} onSave={...} onDelete={...} loading={loading} />`
  - `<ToastContainer />`
- Zdarzenia:
  - mount: wywołanie `loadStatus()`
- Walidacja: brak (formularz dziecka).
- Typy:
  - ViewModel: `ProfileApiKeyState`
- Propsy: brak (strona najwyższego poziomu).

### ApiKeyStatus
- Opis: wyświetla badge z tekstem "Klucz API zapisany" lub "Brak zapisanego klucza API".
- Elementy: `<Badge>` z kolorem zielonym lub szarym.
- Zdarzenia: brak.
- Walidacja: brak.
- Typy:
  - Prop: `hasApiKey: boolean`.

### ApiKeyForm
- Opis: formularz do wprowadzania/aktualizacji/usuwania klucza.
- Elementy:
  - `<Input type="password" label="Klucz API OpenRouter" value={apiKey} onChange={...} />`
  - `<Button onClick={onSave} disabled={loading || apiKey.length===0}>Zapisz klucz</Button>`
  - `<Button onClick={onDelete} disabled={loading || !hasApiKey} variant="destructive">Usuń klucz</Button>`
- Zdarzenia:
  - `onChange` inputu → `setApiKey` w stanie.
  - `onClick` Zapisz → `onSave()`.
  - `onClick` Usuń → `onDelete()`.
- Walidacja:
  - `apiKey.trim().length > 0` → włączony przycisk Zapisz.
- Typy:
  - Props:
    • `apiKey: string`
    • `hasApiKey: boolean`
    • `loading: boolean`
    • `onSave: () => Promise<void>`
    • `onDelete: () => Promise<void>`

### ToastContainer
- Opis: provider dla toastów z Shadcn/ui.
- Elementy: `<Toaster />`.
- Zdarzenia: globalne.
- Walidacja: brak.

## 5. Typy

```ts
// Import z dailogi-api
import { ApiKeyRequest, ApiKeyResponseDTO } from 'dailogi-api/api-keys'

// ViewModel stanu
interface ProfileApiKeyState {
  apiKey: string;
  hasApiKey: boolean;
  loading: boolean;
  error?: string;
}
```

## 6. Zarządzanie stanem
- Hook `useApiKey()`:
  - State: `ProfileApiKeyState`.
  - Metody:
    • `loadStatus(): Promise<void>` – GET status.
    • `saveKey(apiKey: string): Promise<void>` – PUT.
    • `deleteKey(): Promise<void>` – DELETE.
  - Efekt mount: `loadStatus()`.
  - Zarządzanie `loading`, `error`, `hasApiKey`, `apiKey`.

## 7. Integracja API
- GET: `checkApiKeyStatus()` → `has_api_key`.
- PUT: `setApiKey({ api_key })` → zwraca `has_api_key`.
- DELETE: `deleteApiKey()` → zwraca `has_api_key`.
- W hooku: mapować `response.data.has_api_key` do stanu.

## 8. Interakcje użytkownika
1. Wejście na widok → spinner (jeśli `loading`) → załadowanie statusu → wyświetlenie formy i statusu.
2. Wpisanie klucza → przycisk Zapisz aktywny → klik → disable formy, spinner w przycisku → po sukcesie toast "Klucz zapisany" i update status.
3. Klik Usun → modal potwierdzenia (opcjonalnie) → wywołanie DELETE → toast "Klucz usunięty" i update status.
4. Błędy (400,401,500) → toast z komunikatem (`error.response?.data?.message` lub stały).

## 9. Warunki i walidacja
- Klucz nie może być pusty.
- Przyciski disabled gdy `loading`.
- Poza frontendem, middleware na stronie wymusza autoryzację.

## 10. Obsługa błędów
- 400 Bad Request → toast "Nieprawidłowy format klucza".
- 401 Unauthorized → przekierowanie do loginu lub toast "Brak autoryzacji".
- Inne → toast "Wystąpił błąd, spróbuj ponownie".

## 11. Kroki implementacji
1. Utworzyć `ui/src/hooks/useApiKey.ts` z hookiem zarządzającym stanem i wywołaniami API.
2. Dodać stronę `ui/src/pages/profile.astro` lub `.tsx`, import hooka i komponentów.
3. Stworzyć komponenty `ApiKeyStatus.tsx` i `ApiKeyForm.tsx` w `ui/src/components/profile/`.
4. Skonfigurować middleware w `ui/src/middleware/index.ts`, aby chronić `/profile`.
5. Zainstalować i skonfigurować Shadcn/ui ToastProvider w root layout.
6. Dodać style Tailwind i ewentualne tokeny dla kolorów badge.
7. Napisać testy jednostkowe (Vitest+RTL) dla hooka i komponentów.
8. Przetestować end-to-end w Playwright.


