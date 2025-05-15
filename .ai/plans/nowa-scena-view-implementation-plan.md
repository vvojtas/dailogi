# Plan implementacji widoku Nowa Scena

## 1. Przegląd
Widok "Nowa Scena" umożliwia użytkownikowi skonfigurowanie nowej sceny dialogowej: wprowadzenie opisu, wybranie 2–3 postaci oraz przypisanie do każdej modelu LLM. Po zatwierdzeniu użytkownik widzi wskaźnik ładowania generacji, a następnie wynik (dymki dialogu lub komunikat o błędzie) oraz formularz zapisu sceny.

## 2. Routing widoku
Ścieżka Astro:
```text
/ui/src/pages/scenes/new.astro -> /scenes/new
```

## 3. Struktura komponentów
```
NewScenePage (Astro)
└─ NewSceneForm (React)
   ├─ SceneDescriptionInput
   ├─ CharacterSelectionList
   │  └─ CharacterSlot x3
   │     ├─ SelectCharacter
   │     └─ SelectLLM
   ├─ StartSceneButton
   ├─ LoadingIndicator
   ├─ SceneResult (placeholder)
   └─ SaveSceneForm
```

## 4. Szczegóły komponentów

### NewScenePage
- Opis: Strona Astro importująca `NewSceneForm` i renderująca ją po zalogowaniu.
- Dzieci: `NewSceneForm` z `client:load`.
- Wystawione zdarzenia: brak.
- Walidacja: brak.
- Typy: brak.
- Propsy: brak.

### NewSceneForm
- Opis: Kontener zarządzający stanem (opis, postaci, LLM), pobierający dane API i sterujący fazami (konfiguracja → generacja → wynik).
- Główne elementy:
  - `SceneDescriptionInput`
  - `CharacterSelectionList`
  - `StartSceneButton`
  - `LoadingIndicator` (widoczny w fazie generowania)
  - `SceneResult` (widoczny po fazie generowania)
  - `SaveSceneForm` (po generacji)
- Zdarzenia:
  - onChange opisu sceny
  - onSelectCharacter(i, characterId)
  - onSelectLLM(i, llmId)
  - onStartScene()
  - onSaveScene(name)
- Walidacja:
  - minimum 2 i max 3 postaci
  - każda wybrana postać musi mieć wybrany LLM
  - (opcjonalnie) opis sceny nie przekracza 500 znaków
- Typy DTO:
  - `CharacterDTO` (z `getAllAvailableCharacters`)
  - `LLMDTO` (z `getLLMs`)
- ViewModel:
  - `CharacterOption = { id: number; name: string; avatarUrl: string | null }`
  - `LLMOption = { id: number; name: string; }`
  - `SelectedConfig = { characterId?: number; llmId?: number; }[]`
- Propsy: brak, bo sam zarządza stanem.

### SceneDescriptionInput
- Opis: `<textarea>` z etykietą "Opis sceny".
- Elementy: `<label>`, `<textarea>` (Tailwind + Shadcn Form/Textarea).
- Zdarzenia: onChange(value).
- Walidacja: maxLength=500.
- Typy: `string`.
- Propsy: `value: string`, `onChange: (v: string) => void`, `error?: string`.

### CharacterSelectionList
- Opis: Lista 3 slotów postaci
- Elementy: 3x `CharacterSlot`
- Typy: `SelectedConfig`, `CharacterOption[]`, `LLMOption[]`.
- Propsy: `configs`, `characters`, `llms`, `onCharacterChange`, `onLLMChange`, `errors`.

#### CharacterSlot
- Opis: Jeden slot wyboru postaci i modelu LLM.
- Elementy: `Select` (Shadcn) z avatar+nazwą, `Select` z listą LLM.
- Zdarzenia: onChangeCharacter(id), onChangeLLM(id).
- Walidacja: wymagane oba pola, błąd jeżeli brak.
- Typy: `CharacterOption`, `LLMOption`, `number?`.
- Propsy: `index`, `selectedCharacterId`, `selectedLLMId`, `characters`, `llms`, `onCharacterChange`, `onLLMChange`, `errors`.

### AddRemoveCharacterButton
- Opis: Przycisk dodawania lub usuwania trzeciego slotu.
- Elementy: `Button` z ikoną.
- Zdarzenia: onClick add/remove.
- Walidacja: disabled przy min/max.
- Propsy: `canAdd`, `canRemove`, `onAdd`, `onRemove`.

### StartSceneButton
- Opis: Przycisk inicjujący generację.
- Elementy: `Button`.
- Zdarzenia: onClick.
- Walidacja: disabled jeżeli invalid.
- Propsy: `disabled: boolean`, `onClick: () => void`.

### LoadingIndicator
- Opis: Spinner lub Progress bar (Shadcn Spinner).

### SceneResult
- Opis: Placeholder na wynik dialogu.
- Elementy: lista wiadomości ("dymki czatu") lub komunikat o błędzie.
- Zdarzenia: brak.
- Typy: w przyszłości `DialogueEvent[]`, na razie `string` lub `null`.

### SaveSceneForm
- Opis: Formularz nazwy sceny i przycisku "Zapisz scenę".
- Elementy: `Input`, `Button`.
- Zdarzenia: onChangeName, onSave(name).
- Walidacja: nazwa wymagana, maxLength=100.
- Propsy: `defaultName`, `onSave`, `error?`, `disabled?`.

## 5. Typy
- CharacterOption
  - `id: number`
  - `name: string`
  - `avatarUrl: string | null`
- LLMOption
  - `id: number`
  - `name: string`
- SelectedConfig = Array<{ characterId?: number; llmId?: number }>
- NewSceneViewModel
  - `description: string`
  - `configs: SelectedConfig`
  - (w przyszłości) tokeny/dymki dialogu

## 6. Zarządzanie stanem
- Hook `useNewScene`:
  - `characters: CharacterOption[]`
  - `llms: LLMOption[]`
  - `description: string`
  - `configs: SelectedConfig`
  - `phase: 'config' | 'loading' | 'result'`
  - funkcje mutacji (setDescription, setCharacter(i,id), setLLM(i,id), addSlot, removeSlot, startScene)
  - walidacje i `isValid` flag

## 7. Integracja API
- Import z `ui/src/dailogi-api/characters`: `getAllAvailableCharacters()`
- Import z `ui/src/dailogi-api/llm`: `getLLMs()`
- Typy: `CharacterDTO`, `Llmdto`
- Mapowanie API → `CharacterOption[]`, `LLMOption[]`

## 8. Interakcje użytkownika
1. Otwarcie /scenes/new ładuje dropdowny.
2. Użytkownik wpisuje opis.
3. Wybiera 3 postaci i modele.
4. Kliknięcie "Rozpocznij scenę" przełącza widok na fazę generowania.
5. Po generacji wyświetlany wynik.
6. Wpisuje nazwę i klika "Zapisz scenę".

## 9. Warunki i walidacja
- Dokładnie 2-3 sloty postaci muszą być wypełnione (`config.characterId` i `config.llmId` w każdym).
- Opis sceny maxLength 500.
- Nazwa sceny maxLength 100, niepusta.

## 10. Obsługa błędów
- Błąd fetch API: komunikat globalny.
- Błąd walidacji: inline przy polach.
- Błąd generacji (w przyszłości): wyświetlić wygenerowaną część + alert.

## 11. Kroki implementacji
1. Utworzyć plik `ui/src/pages/scenes/new.astro`, import `NewSceneForm`.
2. Utworzyć folder `ui/src/components/scenes`.
3. Dodać komponenty: `NewSceneForm.tsx`, `SceneDescriptionInput.tsx`, `CharacterSelectionList.tsx`, `CharacterSlot.tsx`, `AddRemoveCharacterButton.tsx`, `StartSceneButton.tsx`, `LoadingIndicator.tsx`, `SceneResult.tsx`, `SaveSceneForm.tsx`.
4. W katalogu `ui/src/lib/hooks` utworzyć `useNewScene.ts` z logiką stanu i walidacji.
5. Dodać typy w `ui/src/types.ts` lub `ui/src/types/scenes.ts`.
6. W `NewSceneForm` połączyć hook i komponenty.
7. Stylować formularz za pomocą Tailwind + Shadcn/ui.
8. Przetestować walidacje i przepływy (Minimum: 2 sloty, wymagane LLM, max 3 sloty).
9. (Future) Dodać integrację SSE `/api/dialogues/stream` i wyświetlanie dymków w `SceneResult`.
10. Dodać testy jednostkowe i integracyjne w `ui/src/test` dla komponentów formy. 