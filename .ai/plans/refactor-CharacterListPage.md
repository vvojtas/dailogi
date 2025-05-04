# Refaktoryzacja komponentu CharacterListPage

## 1. Analiza

**Główne funkcjonalności komponentu:**
- Wyświetlanie listy postaci z nagłówkiem, stanem (np. ładowanie, odświeżanie, błąd) oraz stronicowaniem.
- Zarządzanie pobieraniem danych (API call: getCharacters) oraz usuwaniem postaci (API call: deleteCharacter).
- Obsługa navigacji (tworzenie, edycja, przegląd szczegółów) poprzez przekierowania.

**Obecne problemy i obszary o wysokiej złożoności:**
- Wielostronicowa logika zarządzania stanem, gdzie pojawia się sporo useState (dla ładowania, odświeżania, błędów, paginacji, listy postaci, itp.).
- Bezpośrednie wykonywanie API call-i wewnątrz komponentu, co zwiększa złożoność i utrudnia testowanie.
- Rozproszona logika obsługi błędów (użycie toast i DailogiError) w wielu miejscach komponentu.

## 2. Plan refaktoryzacji

### 2.1. Zmiany w strukturze komponentu
- **Ekstrakcja logiki do dedykowanego hooka:**
  - Utworzyć custom hook, np. `useCharacters`, który opakuje logikę pobierania postaci, obsługi błędów, stronicowania oraz usuwania. Dzięki temu główny komponent będzie wyłącznie prezentacyjny.
  
- **Podział na mniejsze komponenty:**
  - Zastanowić się nad wyodrębnieniem elementów prezentacyjnych (np. nagłówek, status, siatka, paginacja) do oddzielnych podkomponentów, aby zwiększyć czytelność głównego komponentu.

### 2.2. Optymalizacja logiki
- **Użycie hooków optymalizacyjnych:**
  - Zachować stosowanie `useCallback` dla funkcji callback (np. `handleDeleteCharacter`, `handlePageChange`, `handleRefresh`) w celu uniknięcia niepotrzebnych renderów.
  - Rozważyć zastosowanie `useMemo` lub `useReducer` w celu uporządkowania złożonej logiki stanu.

- **Centralizacja obsługi błędów:**
  - Wyodrębnić logikę obsługi błędów do osobnej funkcji lub hooka, by zapewnić jednolity sposób wyświetlania komunikatów (z użyciem `DailogiError` i toast).

### 2.3. Zarządzanie wywołaniami API
- **Abstrakcja wywołań API:**
  - Oddzielić wywołania API (getCharacters, deleteCharacter) do osobnego serwisu lub zintegrować je w hooku `useCharacters`. Pozwoli to na lepszą kontrolę nad logiką wywołań oraz ułatwi ewentualne zmiany / cache'owanie.
  - Zapewnić jednolitą obsługę odpowiedzi oraz błędów w centralnej funkcji, redukując duplikację kodu.

### 2.4. Strategia testowania
- **Testy jednostkowe i integracyjne:**
  - Testować nowy custom hook `useCharacters`, symulując różne scenariusze (sukces, błąd, puste dane, paginacja, usuwanie ostatniej pozycji).
  - Zastosować narzędzia takie jak React Testing Library do testowania interakcji w głównym komponencie. 

- **Testowanie obsługi błędów:**
  - Upewnić się, że w przypadku nieudanych wywołań API odpowiednie komunikaty (toast z błędem) są wyświetlane.
  - Sprawdzić, czy przekierowania na odpowiednie strony (tworzenie, edycja, szczegóły) działają poprawnie.

- **Edge cases do przetestowania:**
  - Scenariusze, w których występują błędy sieciowe lub API zwraca błędy, a także sytuacja, gdy usunięcie ostatniej pozycji na stronie powoduje przejście na poprzednią stronę.

## Podsumowanie

Celem refaktoryzacji jest oddzielenie logiki biznesowej od logiki prezentacyjnej, co pozwoli na większą modularność, lepszą testowalność oraz zwiększenie czytelności kodu. Wdrażanie dedykowanego hooka `useCharacters` umożliwi łatwiejsze zarządzanie stanem i wywołaniami API, natomiast wyodrębnienie prezentacyjnych podkomponentów pomoże zachować klarowność głównego komponentu. Dzięki tym zmianom kod stanie się bardziej elastyczny i gotowy na przyszłe rozszerzenia. 