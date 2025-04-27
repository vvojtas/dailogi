# Dokument wymagań produktu (PRD) - d-AI-logi

## 1. Przegląd produktu
d-AI-logi to aplikacja internetowa pozwalająca użytkownikom na tworzenie i obserwowanie interaktywnych dialogów pomiędzy różnymi osobowościami AI. Użytkownicy mogą wybierać z biblioteki gotowych postaci (archetypów lub postaci fikcyjnych) lub tworzyć własne, definiując ich charakterystykę za pomocą opisów. Aplikacja umożliwia zestawienie 2-3 postaci w ramach jednej "sceny", zdefiniowanie tematu rozmowy i obserwowanie automatycznie generowanego dialogu, tura po turze. Każda postać może być napędzana przez inny, wybrany przez użytkownika model językowy (LLM) za pośrednictwem integracji z OpenRouter. Aplikacja oferuje podstawowe funkcje zarządzania kontem, tworzenia i zarządzania własnymi postaciami oraz zapisywania historii przeprowadzonych dialogów.

## 2. Problem użytkownika
Istnieją dwa główne problemy, które d-AI-logi ma na celu rozwiązać:
1.  Złożoność tworzenia interakcji między różnymi AI: Konfiguracja i zarządzanie wieloma modelami AI w celu symulowania rozmowy wymaga wiedzy technicznej i osobnych ustawień dla każdego modelu. Brakuje prostego narzędzia do łatwego zestawiania różnych osobowości AI i obserwowania ich dynamicznych interakcji.
2.  Brak narzędzia dla fanów fikcji: Miłośnicy różnych uniwersów (książek, filmów, gier) nie mają łatwo dostępnego sposobu na tworzenie i eksplorowanie hipotetycznych dialogów między swoimi ulubionymi postaciami, co mogłoby służyć rozrywce lub inspiracji twórczej (np. dla pisarzy).

## 3. Wymagania funkcjonalne
Aplikacja w wersji MVP (Minimum Viable Product) będzie posiadała następujące funkcjonalności:

*   **FR-001: Zarządzanie kontem użytkownika:**
    *   Rejestracja nowych użytkowników.
    *   Logowanie istniejących użytkowników.
*   **FR-002: Zarządzanie postaciami AI:**
    *   Tworzenie nowej, własnej postaci AI przez użytkownika (limit 50 na użytkownika).
        *   Wymagane pola: Nazwa, Opis (do 500 słów), Skrócony opis.
        *   Opcjonalne pole: Awatar (JPG/PNG, max 1MB, do 256x256 px).
    *   Edycja istniejących własnych postaci AI.
    *   Usuwanie własnych postaci AI.
    *   Przeglądanie listy własnych postaci AI.
*   **FR-003: Biblioteka postaci predefiniowanych:**
    *   Dostęp do niewielkiej biblioteki gotowych postaci (np. z domeny publicznej, bajek) z predefiniowanymi awatarami.
*   **FR-004: Tworzenie i konfiguracja sceny dialogowej:**
    *   Możliwość rozpoczęcia tworzenia nowej sceny.
    *   Wybór 2 lub 3 postaci (własnych lub z biblioteki) do udziału w scenie.
    *   Wprowadzenie tematu/opisu sceny (okoliczności rozmowy).
    *   Wybór modelu LLM (z predefiniowanej listy: Google Gemini Flash, Anthropic Claude Sonnet, DeepSeek V3, Meta Llama 70B, OpenAI o3 Min) dla każdej postaci uczestniczącej w scenie, za pośrednictwem OpenRouter.
*   **FR-005: Generowanie i obserwacja dialogu:**
    *   Inicjacja automatycznego generowania dialogu przyciskiem.
    *   Dialog generowany jest tura po turze, w trybie round-robin (postać A -> postać B -> postać C -> postać A...).
    *   Maksymalna długość dialogu: 50 tur (łącznie dla wszystkich postaci).
    *   Wizualizacja dialogu w formie dymków czatu, z widoczną nazwą i awatarem postaci mówiącej.
    *   Brak możliwości interwencji użytkownika lub zatrzymania dialogu w trakcie jego generowania.
*   **FR-006: Zarządzanie historią dialogów:**
    *   Automatyczne zapisywanie wygenerowanego dialogu po jego zakończeniu (limit 50 zapisanych dialogów na użytkownika).
    *   Przeglądanie listy zapisanych dialogów.
    *   Możliwość otwarcia i odczytania zapisanego dialogu.
*   **FR-007: Zarządzanie kluczem API OpenRouter:**
    *   Możliwość wprowadzenia i zapisania własnego klucza API OpenRouter w profilu użytkownika.
    *   Klucz API przechowywany w bazie danych w formie zaszyfrowanej (AES-GCM).
*   **FR-008: Mechanizm "Specjalnego Użytkownika":**
    *   Możliwość oznaczenia użytkownika jako "specjalnego" (ręczna zmiana flagi w bazie danych przez administratora).
    *   Specjalni użytkownicy mogą korzystać z aplikacji używając globalnego, ograniczonego klucza API aplikacji, bez konieczności podawania własnego.
*   **FR-009: Obsługa błędów API:**
    *   Wyświetlanie użytkownikowi czytelnych komunikatów w przypadku błędów związanych z API OpenRouter (np. nieprawidłowy klucz, brak środków, błąd serwera OpenRouter).
    *   W przypadku błędu podczas generowania dialogu, wyświetlenie dotychczas wygenerowanej części dialogu wraz z informacją o błędzie.
*   **FR-010: Interfejs użytkownika:**
    *   Prosta strona startowa po zalogowaniu, zawierająca linki do głównych sekcji (lista postaci, historia dialogów, tworzenie nowej sceny).

## 4. Granice produktu (Zakres MVP)

### Funkcjonalności wchodzące w zakres MVP:
*   Wszystkie funkcjonalności wymienione w sekcji "Wymagania funkcjonalne" (FR-001 do FR-010).
*   Podstawowa biblioteka postaci.
*   Tworzenie prostych postaci (nazwa, opis, skrócony opis, awatar).
*   Wybór LLM per postać z predefiniowanej listy przez OpenRouter.
*   Automatyczna generacja dialogu (max 50 tur, round-robin).
*   Zapis i odczyt historii dialogów (limit 50).
*   System kont (rejestracja/logowanie).
*   Zarządzanie własnym kluczem API OpenRouter.
*   Mechanizm specjalnego użytkownika (dostęp przez flagę w bazie).

### Funkcjonalności NIE wchodzące w zakres MVP:
*   Zaawansowane tworzenie postaci (np. trenowanie na tekstach, długie definicje, parametryzacja suwakami).
*   Zaawansowana konfiguracja parametrów LLM (np. temperatura, top_p, max tokens).
*   Interakcje więcej niż 3 postaci jednocześnie.
*   Możliwość zatrzymania generowania dialogu przez użytkownika.
*   Interwencja użytkownika w trakcie generowanego dialogu.
*   System ocen, rekomendacji, udostępniania postaci lub scen między użytkownikami.
*   Eksport dialogów do różnych formatów.
*   Wsparcie dla modeli lokalnych lub niestandardowych endpointów AI.
*   Moderacja treści / filtrowanie odpowiedzi LLM.
*   Wyszukiwanie w historii dialogów.
*   Wyświetlanie informacji o koszcie/charakterystyce modeli LLM.
*   Zaawansowana obsługa przekroczenia limitu kontekstu LLM.

## 5. Historyjki użytkowników

### Zarządzanie Kontem

*   ID: US-001
*   Tytuł: Rejestracja nowego użytkownika
*   Opis: Jako nowy użytkownik, chcę móc założyć konto w aplikacji, podając nazwę użytkownika i hasło, abym mógł korzystać z jej funkcjonalności.
*   Kryteria akceptacji:
    *   Formularz rejestracji zawiera pola na nazwę użytkownika i hasło (z potwierdzeniem).
    *   Walidacja sprawdza poprawność nazwy użytkownika i minimalną długość hasła.
    *   Po pomyślnej rejestracji użytkownik jest informowany o sukcesie i może się zalogować.
    *   W przypadku błędu (np. zajęta nazwa użytkownika) wyświetlany jest odpowiedni komunikat.

*   ID: US-002
*   Tytuł: Logowanie do aplikacji
*   Opis: Jako zarejestrowany użytkownik, chcę móc zalogować się do aplikacji przy nazwy użytkownika i hasła, abym mógł uzyskać dostęp do moich postaci, historii dialogów i tworzyć nowe sceny.
*   Kryteria akceptacji:
    *   Formularz logowania zawiera pola na nazwę użytkownika i hasło.
    *   Po pomyślnym zalogowaniu użytkownik jest przekierowany na stronę startową aplikacji.
    *   W przypadku podania błędnych danych logowania wyświetlany jest odpowiedni komunikat.
    *   Sesja użytkownika jest utrzymywana po zalogowaniu.

*   ID: US-003
*   Tytuł: Wprowadzenie klucza API OpenRouter
*   Opis: Jako zalogowany użytkownik, chcę móc wprowadzić i zapisać mój własny klucz API OpenRouter w ustawieniach profilu, aby móc korzystać z modeli LLM w ramach własnych limitów i rozliczeń OpenRouter.
*   Kryteria akceptacji:
    *   W profilu użytkownika znajduje się pole do wprowadzenia klucza API OpenRouter.
    *   Po zapisaniu klucz jest przechowywany w bazie danych w sposób zaszyfrowany.
    *   Istnieje możliwość zaktualizowania lub usunięcia zapisanego klucza.
    *   Interfejs informuje, czy klucz został pomyślnie zapisany.

*   ID: US-004
*   Tytuł: Korzystanie z globalnego klucza API (Specjalny Użytkownik)
*   Opis: Jako zalogowany użytkownik specjalny (z odpowiednią flagą w bazie), chcę móc korzystać z aplikacji bez podawania własnego klucza API, wykorzystując limitowany klucz globalny aplikacji.
*   Kryteria akceptacji:
    *   Jeśli użytkownik ma flagę "specjalny" i nie wprowadził własnego klucza, aplikacja używa klucza globalnego do zapytań do OpenRouter.
    *   Jeśli użytkownik specjalny wprowadzi własny klucz, będzie on używany zamiast globalnego.
    *   Zwykły użytkownik bez własnego klucza nie może generować dialogów (lub widzi stosowny komunikat).

### Zarządzanie Postaciami

*   ID: US-005
*   Tytuł: Tworzenie nowej postaci AI
*   Opis: Jako zalogowany użytkownik, chcę móc stworzyć nową, własną postać AI, podając jej nazwę, pełny opis charakteru (do 500 słów) i skrócony opis, abym mógł jej używać w scenach dialogowych.
*   Kryteria akceptacji:
    *   Formularz tworzenia postaci zawiera pola na: Nazwę (tekst, wymagane), Opis (textarea, wymagane, limit 500 słów), Skrócony Opis (tekst, wymagane).
    *   Walidacja sprawdza, czy wymagane pola są wypełnione i czy opis nie przekracza limitu znaków.
    *   Po pomyślnym utworzeniu postać pojawia się na liście moich postaci.
    *   W przypadku błędu walidacji wyświetlany jest odpowiedni komunikat.

*   ID: US-006
*   Tytuł: Dodawanie awatara do postaci
*   Opis: Jako zalogowany użytkownik, podczas tworzenia lub edycji postaci, chcę móc opcjonalnie wgrać plik graficzny (JPG/PNG, max 1MB, 256x256px) jako awatar mojej postaci, aby nadać jej wizualną reprezentację.
*   Kryteria akceptacji:
    *   Formularz tworzenia/edycji postaci zawiera opcjonalne pole do wgrania pliku awatara.
    *   Walidacja sprawdza format pliku (JPG/PNG), rozmiar (max 1MB) i wymiary (max 256x256px).
    *   Po pomyślnym wgraniu, awatar jest zapisywany i powiązany z postacią.
    *   Awatar jest wyświetlany obok nazwy postaci na listach i w oknie dialogu.
    *   Jeśli awatar nie zostanie wgrany, używany jest domyślny placeholder.

*   ID: US-007
*   Tytuł: Przeglądanie listy własnych postaci
*   Opis: Jako zalogowany użytkownik, chcę móc przejrzeć listę wszystkich stworzonych przeze mnie postaci AI, widząc ich nazwy i awatary, abym mógł zarządzać moją kolekcją.
*   Kryteria akceptacji:
    *   Istnieje dedykowana sekcja/strona wyświetlająca listę postaci użytkownika.
    *   Każda pozycja na liście pokazuje co najmniej nazwę i awatar (lub placeholder) postaci.
    *   Lista obsługuje paginację lub przewijanie, jeśli liczba postaci przekracza określoną wartość (np. 10 na stronę).
    *   Przy każdej postaci na liście znajdują się opcje edycji i usunięcia.

*   ID: US-008
*   Tytuł: Edycja istniejącej postaci AI
*   Opis: Jako zalogowany użytkownik, chcę móc edytować stworzoną przeze mnie postać AI, zmieniając jej nazwę, opis, skrócony opis lub awatar, abym mógł poprawić lub zaktualizować jej dane.
*   Kryteria akceptacji:
    *   Z poziomu listy postaci mogę przejść do formularza edycji wybranej postaci.
    *   Formularz edycji jest wstępnie wypełniony aktualnymi danymi postaci.
    *   Mogę zmodyfikować pola Nazwa, Opis, Skrócony Opis oraz wgrać nowy Awatar.
    *   Walidacja (jak przy tworzeniu) jest stosowana przy zapisywaniu zmian.
    *   Po pomyślnym zapisaniu zmiany są odzwierciedlone na liście postaci i w przyszłych dialogach.

*   ID: US-009
*   Tytuł: Usuwanie postaci AI
*   Opis: Jako zalogowany użytkownik, chcę móc usunąć stworzoną przeze mnie postać AI, abym mógł pozbyć się niepotrzebnych lub błędnych postaci z mojej kolekcji.
*   Kryteria akceptacji:
    *   Z poziomu listy postaci mogę zainicjować proces usuwania wybranej postaci.
    *   Wyświetlane jest potwierdzenie z pytaniem, czy na pewno chcę usunąć postać.
    *   Po potwierdzeniu postać jest trwale usuwana z bazy danych.
    *   Postać znika z listy moich postaci.

### Tworzenie Sceny i Generowanie Dialogu

*   ID: US-010
*   Tytuł: Rozpoczęcie tworzenia nowej sceny
*   Opis: Jako zalogowany użytkownik, chcę móc rozpocząć proces tworzenia nowej sceny dialogowej z poziomu strony startowej lub dedykowanego przycisku, abym mógł wybrać postacie i zdefiniować temat rozmowy.
*   Kryteria akceptacji:
    *   Istnieje przycisk/link "Utwórz nową scenę" (lub podobny) dostępny dla zalogowanego użytkownika.
    *   Kliknięcie przenosi do interfejsu konfiguracji sceny.

*   ID: US-011
*   Tytuł: Wybór postaci do sceny
*   Opis: Jako zalogowany użytkownik, podczas konfiguracji sceny, chcę móc wybrać od 2 do 3 postaci spośród moich własnych postaci oraz postaci z biblioteki predefiniowanej, aby określić uczestników dialogu.
*   Kryteria akceptacji:
    *   Interfejs pozwala na przeglądanie dostępnych postaci (własnych i z biblioteki).
    *   Mogę wybrać minimum 2 i maksimum 3 postacie.
    *   Wybrane postacie są wyraźnie oznaczone.
    *   Interfejs uniemożliwia wybór mniej niż 2 lub więcej niż 3 postaci.

*   ID: US-012
*   Tytuł: Definiowanie tematu/opisu sceny
*   Opis: Jako zalogowany użytkownik, podczas konfiguracji sceny, chcę móc wprowadzić krótki opis tematu rozmowy lub scenerii, w której ma się ona odbywać, aby nadać kontekst dialogowi generowanemu przez AI.
*   Kryteria akceptacji:
    *   Istnieje pole tekstowe (textarea) do wprowadzenia opisu sceny.
    *   Wprowadzony opis zostanie przekazany do modeli LLM jako część promptu systemowego.
    *   Pole może być opcjonalne lub wymagane (do decyzji).

*   ID: US-013
*   Tytuł: Wybór modelu LLM dla postaci w scenie
*   Opis: Jako zalogowany użytkownik, podczas konfiguracji sceny, dla każdej wybranej postaci chcę móc wybrać model językowy (LLM) z dostępnej listy (np. Gemini Flash, Claude Sonnet), który będzie odpowiedzialny za generowanie wypowiedzi tej postaci.
*   Kryteria akceptacji:
    *   Przy każdej wybranej postaci w interfejsie konfiguracji sceny znajduje się lista rozwijana (lub inny element wyboru).
    *   Lista zawiera predefiniowane modele LLM dostępne przez OpenRouter (Gemini Flash, Claude Sonnet, DeepSeek V3, Llama 70B, o3 Min).
    *   Domyślnie może być wybrany pierwszy model z listy lub żaden (wymagający wyboru).
    *   Dokonany wybór jest zapisywany dla danej postaci w kontekście tworzonej sceny.

*   ID: US-014
*   Tytuł: Inicjacja generowania dialogu
*   Opis: Jako zalogowany użytkownik, po skonfigurowaniu sceny (wybraniu postaci, opcjonalnym opisie, wyborze LLM), chcę móc kliknąć przycisk "Rozpocznij dialog" (lub podobny), aby zainicjować automatyczne generowanie rozmowy.
*   Kryteria akceptacji:
    *   Przycisk inicjujący jest dostępny po spełnieniu minimalnych warunków konfiguracji sceny (wybrane 2-3 postacie, wybrane LLM dla każdej).
    *   Kliknięcie przycisku rozpoczyna proces komunikacji z API OpenRouter i generowania dialogu tura po turze.
    *   Użytkownik jest przenoszony do widoku obserwacji dialogu.

*   ID: US-015
*   Tytuł: Obserwacja generowanego dialogu
*   Opis: Jako zalogowany użytkownik, po zainicjowaniu generowania dialogu, chcę móc obserwować pojawiające się wypowiedzi postaci w czasie rzeczywistym (lub z niewielkim opóźnieniem), wyświetlane w formie dymków czatu z awatarem i nazwą postaci.
*   Kryteria akceptacji:
    *   Widok dialogu prezentuje rozmowę w formie czatu (dymki).
    *   Każdy dymek zawiera treść wypowiedzi, nazwę postaci mówiącej i jej awatar (lub placeholder).
    *   Nowe wypowiedzi pojawiają się automatycznie w miarę ich generowania przez LLM (tura po turze, round-robin).
    *   Dialog kończy się automatycznie po osiągnięciu limitu 50 tur lub w przypadku błędu.

*   ID: US-016
*   Tytuł: Obsługa błędu podczas generowania dialogu
*   Opis: Jako zalogowany użytkownik, jeśli podczas generowania dialogu wystąpi błąd komunikacji z API OpenRouter (np. nieprawidłowy klucz, brak środków, błąd serwera), chcę zobaczyć dotychczas wygenerowaną część dialogu oraz czytelny komunikat o błędzie.
*   Kryteria akceptacji:
    *   W przypadku błędu API, proces generowania dialogu jest przerywany.
    *   W widoku dialogu wyświetlana jest cała rozmowa wygenerowana przed wystąpieniem błędu.
    *   Pod dialogiem (lub w innym widocznym miejscu) pojawia się komunikat informujący o rodzaju błędu (np. "Błąd API OpenRouter: Nieprawidłowy klucz API.", "Generowanie przerwane z powodu błędu serwera.").
    *   Dialog w takim stanie również jest zapisywany w historii.

### Historia Dialogów

*   ID: US-017
*   Tytuł: Zapisywanie zakończonego dialogu
*   Opis: Jako zalogowany użytkownik, chcę móc wybrane najciekawsze dialogi zapisać w mojej historii dialogów (do limitu 50), abym mógł do nich wrócić później.
*   Kryteria akceptacji:
    *   Istnieje przycisk inicjujący zapis zakończonego dialogu (po osiągnięciu limitu tur lub błądzie)
    *   Użytkownik podaje nazwę pod którą chce zapisać dialog
    *   Cała konwersacja wraz z informacją o uczestniczących postaciach i opisie sceny jest zapisywana w bazie danych powiązanej z użytkownikiem.
    *   Jeśli użytkownik osiągnął limit 50 zapisanych dialogów, zapis nowego dialogu jest blokowany.

*   ID: US-018
*   Tytuł: Przeglądanie historii dialogów
*   Opis: Jako zalogowany użytkownik, chcę móc przejrzeć listę moich zapisanych dialogów, widząc podstawowe informacje o każdym z nich (np. uczestniczące postacie, data), abym mógł łatwo znaleźć interesującą mnie rozmowę.
*   Kryteria akceptacji:
    *   Istnieje dedykowana sekcja/strona "Historia dialogów".
    *   Lista wyświetla zapisane dialogi w porządku chronologicznym (od najnowszego do najstarszego).
    *   Każda pozycja na liście zawiera informacje identyfikujące dialog, np. nazwy uczestniczących postaci, datę zapisu, ewentualnie początek opisu sceny.
    *   Lista obsługuje paginację lub przewijanie, jeśli liczba dialogów przekracza określoną wartość.

*   ID: US-019
*   Tytuł: Odczytywanie zapisanego dialogu
*   Opis: Jako zalogowany użytkownik, chcę móc otworzyć zapisany dialog z listy historii, aby ponownie przeczytać całą rozmowę, która się odbyła.
*   Kryteria akceptacji:
    *   Kliknięcie na pozycję dialogu na liście historii otwiera widok tego konkretnego dialogu.
    *   Widok prezentuje całą zapisaną konwersację w formacie czatu (dymki z awatarami/nazwami), identycznym jak podczas obserwacji generowania.
    *   Wyświetlane są również informacje o scenie (uczestnicy, opis, użyte LLM).

*   ID: US-020
*   Tytuł: Usuwanie dialogu
*   Opis: Jako zalogowany użytkownik, chcę móc usunąć zapisany przeze mnie dialog, abym mógł pozbyć się niepotrzebnych lub wadliwych wpisów z mojej kolekcji.
*   Kryteria akceptacji:
    *   Z poziomu listy dialogów mogę zainicjować proces usuwania wybranego dialogu.
    *   Wyświetlane jest potwierdzenie z pytaniem, czy na pewno chcę usunąć dialog.
    *   Po potwierdzeniu dialog jest trwale usuwany z bazy danych.
    *   Dialog znika z listy moich dialogów.

### Niezalogowany Użytkownik (Gość)

*   ID: US-021
*   Tytuł: Próba akcji wymagającej zalogowania
*   Opis: Jako niezalogowany użytkownik (gość), próbując wykonać akcję dostępną tylko dla zalogowanych (np. stworzenie postaci, rozpoczęcie dialogu, zapisanie klucza API), chcę zostać poinformowany o konieczności zalogowania lub rejestracji i przekierowany do odpowiedniego formularza.
*   Kryteria akceptacji:
    *   Przyciski/linki do akcji wymagających autentykacji (np. "Utwórz postać", "Utwórz scenę", "Mój profil") są nieaktywne lub po kliknięciu prowadzą do strony logowania/rejestracji.
    *   Wyświetlany jest komunikat wyjaśniający, że dana funkcja wymaga konta.

*   ID: US-022
*   Tytuł: Przeglądanie biblioteki postaci predefiniowanych
*   Opis: Jako niezalogowany użytkownik (gość), chcę móc przejrzeć listę dostępnych postaci predefiniowanych (np. z domeny publicznej), widząc ich nazwy i awatary, abym mógł zobaczyć, jakie postacie są dostępne w aplikacji przed założeniem konta.
*   Kryteria akceptacji:
    *   Nawet bez logowania, istnieje możliwość dostępu do sekcji/widoku z biblioteką postaci predefiniowanych.
    *   Lista wyświetla nazwy i awatary (lub placeholdery) postaci globalnych.
    *   Nie widzę opcji edycji ani usuwania tych postaci.
    *   Nie widzę postaci stworzonych przez innych użytkowników.

*   ID: US-023
*   Tytuł: Przeglądanie szczegółów postaci predefiniowanej
*   Opis: Jako niezalogowany użytkownik (gość), po znalezieniu interesującej postaci w bibliotece predefiniowanej, chcę móc zobaczyć jej szczegóły (pełny opis, skrócony opis, awatar), aby dowiedzieć się o niej więcej.
*   Kryteria akceptacji:
    *   Z poziomu listy postaci predefiniowanych mogę przejść do widoku szczegółów wybranej postaci.
    *   Widok szczegółów wyświetla nazwę, pełny opis, skrócony opis i awatar (lub placeholder) postaci.
    *   Nie widzę opcji edycji ani usuwania dla tej postaci.

## 6. Metryki sukcesu
Sukces wersji MVP będzie mierzony za pomocą następujących kryteriów:

*   MS-001: Średni czas spędzony przez użytkownika w aplikacji podczas jednej sesji:
    *   Cel: Minimum 5 minut.
    *   Pomiar: Czas od logowania do ostatniej akcji w aplikacji lub wylogowania w ramach jednej sesji użytkownika.
*   MS-002: Odsetek użytkowników tworzących własne postacie AI:
    *   Cel: 20% zarejestrowanych użytkowników tworzy przynajmniej jedną własną postać AI.
    *   Pomiar: (Liczba użytkowników z co najmniej jedną własną postacią / Całkowita liczba zarejestrowanych użytkowników) * 100%.
*   MS-003: Retencja tygodniowa użytkowników:
    *   Cel: 50% zarejestrowanych użytkowników wraca do aplikacji w ciągu tygodnia od pierwszego użycia (rejestracji).
    *   Pomiar: (Liczba użytkowników, którzy zalogowali się ponownie w ciągu 7 dni od daty rejestracji / Całkowita liczba zarejestrowanych użytkowników w danym okresie) * 100%. 