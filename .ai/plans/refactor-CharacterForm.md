# Plan Refaktoryzacji Komponentu CharacterForm

Niniejszy dokument zawiera szczegółowy plan refaktoryzacji komponentu `CharacterForm`. Celem refaktoryzacji jest poprawa struktury, przejrzystości oraz utrzymania kodu, a także usprawnienie obsługi formularza z użyciem React Hook Form i lepsze zarządzanie wywołaniami API.

## 1. Analiza

- **Komponent:** CharacterForm
- **Główne funkcjonalności:**
  - Renderowanie pól formularza (nazwa, krótki opis, biografia, wybór modelu językowego, ładowanie awatara).
  - Zarządzanie stanem formularza przy użyciu React Hook Form i walidacji z wykorzystaniem Zod.
  - Obsługa stanu ładowania oraz mechanizmu "hydration" (dezaktywacja pól do pełnego załadowania komponentu).
  - Logika wywołań API: tworzenie nowej postaci (createCharacter) oraz aktualizacja istniejącej (updateCharacter).
  - Obsługa błędów oraz wyświetlanie komunikatów toast przy użyciu DailogiError.

## 2. Plan refaktoryzacji

### 2.1. Zmiany w strukturze komponentu
- Rozdzielić logikę prezentacyjną (UI) od logiki formularza.
- Wydzielić złożone funkcje pomocnicze, np. przetwarzanie pliku awatara do base64 (funkcja `fileToBase64`), do osobnych modułów.
- Uporządkować importy oraz stałe wartości używane przez formularz (np. domyślne wartości pól).

### 2.2. Implementacja React Hook Form
- Użyj `useForm` z React Hook Form do zarządzania stanem formularza i walidacji danych, wykorzystując zodResolver dla walidacji schematu.
- Rozważ użycie komponentu `Controller` dla pól wymagających niestandardowego renderowania lub zarządzania (np. wybór pliku, Select).
- Zachowaj logikę stanu "hydration", aby pola formularza były aktywne dopiero po zakończeniu procesu ładowania.

### 2.3. Optymalizacja logiki komponentu
- Upraszczaj funkcję `onSubmit` poprzez:
  - Wydzielenie logiki przetwarzania awatara (konwersja pliku do base64) do oddzielnej funkcji pomocniczej.
  - Oddzielenie logiki aktualizacji i tworzenia postaci. Rozważ stworzenie dedykowanej funkcji, np. `performSubmit`, która wybiera odpowiednią ścieżkę ze względu na obecność `initialData`.
  - Redukcję zagnieżdżeń try-catch i zastosowanie wczesnych returnów w przypadku wykrycia błędu.

### 2.4. Zarządzanie wywołaniami API
- Przenieść wywołania API (`createCharacter` i `updateCharacter`) do osobnego serwisu lub dedykowanego hooka (np. `useCharacterApi`).
- Centralizować obsługę błędów i komunikatów toast tak, aby uniknąć duplikacji logiki w różnych ścieżkach.
- Rozważyć integrację z React Query lub SWR do zarządzania stanem ładowania, cache'owaniem oraz ponawianiem zapytań.

### 2.5. Strategia testowania
- **Unit Testy:**
  - Testowanie walidacji formularza za pomocą narzędzi takich jak React Testing Library oraz narzędzi testowych dla Zod.
  - Testy funkcji pomocniczych, np. konwersji pliku do base64 i obsługi błędów.
- **Testy Integracyjne:**
  - Przetestowanie przepływu formularza, od wprowadzenia danych, przez walidację, po wywołanie API przy użyciu symulowanych danych.
- **Testy End-to-End:**
  - Upewnienie się, że interakcje (np. kliknięcia, przesłanie formularza) działają tylko po pełnym załadowaniu komponentu (stan hydration).
  - Sprawdzenie, że komunikaty toast pojawiają się zgodnie z oczekiwaniami w przypadku sukcesu oraz błędów.

## 3. Dodatkowe rekomendacje
- Zachować spójność komunikatów, stosując dramatyczne i nieszablonowe sformułowania zgodne z wytycznymi projektu (np. "Powołaj do życia" zamiast "Utwórz").
- Regularnie dokonywać przeglądu kodu oraz wprowadzać usprawnienia wynikające z feedbacku zespołu.
- Aktualizować dokumentację komponentu, szczególnie po wprowadzeniu znaczących zmian, aby ułatwić przyszłe modyfikacje i utrzymanie.

---

**Podsumowanie**

Plan refaktoryzacji ma na celu uporządkowanie logiki formularza w komponencie `CharacterForm`, poprawiając jego modularność, czytelność i testowalność. Wykorzystanie dedykowanych hooków, komponentów oraz zarządzanie wywołaniami API w osobnych modułach umożliwi lepsze utrzymanie kodu, a także dostosowanie komponentu do dynamicznych wymagań projektu D-AI-Logi. 