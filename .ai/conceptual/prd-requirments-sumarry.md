<conversation_summary>
<decisions>
1.  **Grupa Docelowa:** Głównie fani fikcji i pisarze szukający inspiracji; dla osób technicznych jako ciekawostka.
2.  **Tworzenie Postaci:** Umożliwienie tworzenia postaci na podstawie krótkich opisów tropów oraz złożonych opisów do 500 słów. Limit 50 własnych postaci na użytkownika. Walidacja: pola nazwa, opis, skrócony opis muszą być wypełnione.
3.  **Biblioteka Postaci:** Początkowa biblioteka ma zawierać kilka gotowych postaci (np. Czerwony Kapturek, Cezar, Sherlock Holmes) z domeny publicznej lub bajek, by unikać naruszeń praw autorskich. Główny nacisk na tworzenie własnych postaci przez użytkowników.
4.  **Silnik AI (LLM):** Wykorzystanie OpenRouter do przełączania modeli. Początkowa lista modeli: Google Gemini 2.0 Flash, Anthropic Claude 3.7 Sonnet, DeepSeek DeepSeek V3, Meta Llama 3.3 70B Instruct, OpenAI o3 Min. Wybór modelu przez listę rozwijaną na widoku sceny.
5.  **Generowanie Dialogu:** Automatyczne generowanie tura po turze (max 50 tur) w trybie round-robin. Użytkownik inicjuje dialog przyciskiem (np. "Porozmawiajcie sobie"). Zatrzymać dialog w trakcie nie można w MVP.
6.  **Kontekst dla LLM:** Do LLM sterującego daną postacią wysyłany jest jej pełny opis, skrócone opisy pozostałych postaci uczestniczących w scenie, opis sceny (jako część promptu systemowego) oraz historia dialogu.
7.  **Historia Dialogów:** Zapisywanie i odczytywanie dialogów (limit 50 na użytkownika). Prezentacja jako lista. Wyszukiwanie poza zakresem MVP.
8.  **Moderacja Treści:** Brak moderacji i filtrowania treści w MVP.
9.  **Sterowanie Postacią:** Charakter wypowiedzi wynika wyłącznie z opisu postaci. Brak zaawansowanych opcji sterowania w MVP.
10. **Interfejs Dialogu:** Dymki czatu z nazwami i awatarami postaci.
11. **Awatary:** Użytkownicy mogą wgrywać własne awatary (JPG/PNG, max 1MB, do 256x256 px). Postaci z biblioteki mają predefiniowane awatary. Awatar jest opcjonalny przy tworzeniu postaci.
12. **Klucze API (OpenRouter):** Użytkownicy podają własny klucz API OpenRouter w profilu. Klucz jest szyfrowany w bazie (AES-GCM). "Specjalni użytkownicy" (admin, wybrani) mogą używać ograniczonego klucza globalnego aplikacji (dostęp nadawany ręcznie flagą w bazie).
13. **Śledzenie Sukcesu:** Mierzenie czasu sesji (od logowania do ostatniej interakcji/wylogowania), % użytkowników tworzących postać, % użytkowników powracających w ciągu tygodnia. Brak śledzenia średniej liczby dialogów, użycia modeli LLM, popularności postaci w MVP.
14. **Plan Awaryjny:** Brak (aplikacja zaliczeniowa).
15. **Harmonogram i Zasoby:** 6 tygodni, 1 deweloper + AI.
16. **Obsługa Błędów API:** Komunikaty o nieprawidłowym kluczu, braku środków, ogólnym błędzie OpenRouter. W przypadku błędu w trakcie generowania dialogu, wyświetlenie częściowego dialogu, komunikatu błędu i informacji o błędzie systemowym.
17. **Strona Startowa:** Po zalogowaniu wyświetla linki do: listy postaci, historii dialogów, rozpoczęcia nowego dialogu.

</decisions>

<matched_recommendations>
1.  **Priorytetyzacja LLM:** Zgodne z decyzją o wykorzystaniu OpenRouter i zdefiniowaniu początkowej listy modeli.
2.  **Prostota Tworzenia Postaci:** Zgodne z decyzją o tworzeniu postaci przez opis, z limitem 500 słów i prostą walidacją.
3.  **UX Dialogu:** Zgodne z decyzją o użyciu dymków czatu z nazwami/avatarami.
4.  **Techniczne Podstawy:** Zgodne z decyzją o użyciu OpenRouter, generowaniu round-robin i zdefiniowaniu przepływu dialogu.
5.  **Definicja Metryk:** Zgodne z określeniem mierzonych kryteriów sukcesu (czas sesji, tworzenie postaci, retencja).
6.  **Zarządzanie Długimi Opisami:** Zgodne z decyzją o wysyłaniu pełnego opisu postaci do LLM.
7.  **Limit Tur Dialogu:** Zgodne z decyzją o limicie 50 tur.
8.  **Avatary Postaci:** Zgodne z decyzją o wgrywaniu własnych awatarów i predefiniowanych dla biblioteki, wraz ze specyfikacją.
9.  **Obsługa Klucza API Użytkownika:** Zgodne z decyzją o przechowywaniu zaszyfrowanego klucza użytkownika w profilu.
10. **Dostęp Specjalny:** Zgodne z decyzją o ręcznym nadawaniu dostępu flagą w bazie.
11. **Obsługa Błędów API:** Zgodne z decyzją o typach komunikatów błędów.
12. **Konfiguracja Postaci Startowych:** Zgodne z decyzją o dodaniu postaci z domeny publicznej/bajek (opisy do zdefiniowania).
13. **Dokumentacja Funkcji Odsuniętych:** Zgodne z wielokrotnym wskazaniem funkcji poza zakresem MVP.
14. **Kontekst Między Postaciami:** Zgodne z decyzją o przekazywaniu pełnego opisu danej postaci oraz skróconych opisów pozostałych postaci i opisu sceny.
15. **Walidacja Tworzenia Postaci:** Zgodne z decyzją o prostej walidacji pól.
16. **Specyfikacja Awatarów:** Zgodne z podanymi ograniczeniami formatu, rozmiaru, wymiarów.
17. **Szyfrowanie Kluczy API:** Zgodne z wyborem AES-GCM.
18. **Mechanizm "Specjalnego Użytkownika":** Zgodne z decyzją o ręcznej zmianie flagi.
19. **Struktura Promptu Kontekstowego:** Zgodne z decyzją o przekazywaniu opisów, sceny i historii jako części promptu systemowego. *Uwaga: Rekomendacja dotycząca zarządzania limitem kontekstu została odrzucona dla MVP.*
20. **Logika Tur:** Zgodne z decyzją o round-robin.
21. **Obsługa Błędów w Trakcie Dialogu:** Zgodne z decyzją o wyświetlaniu częściowego dialogu i komunikatu błędu.
22. **Doświadczenie Początkowe (Onboarding):** Zgodne z decyzją o prostej stronie startowej z linkami.
23. **Ryzyko Limitu Kontekstu:** *Rekomendacja została zanotowana, ale obsługa limitu kontekstu została świadomie przeniesiona poza MVP.*
24. **Dokumentacja Ryzyka Braku Filtrowania:** Zgodne z decyzją o braku moderacji (ryzyko do udokumentowania w PRD).

</matched_recommendations>

<prd_planning_summary>
**a. Główne wymagania funkcjonalne:**
    *   System kont użytkowników (rejestracja/logowanie).
    *   Tworzenie, edycja, usuwanie własnych postaci AI (do 50 na użytkownika) z nazwą, opisem (do 500 słów), skróconym opisem i opcjonalnym awatarem (JPG/PNG, max 1MB, 256x256px). Prosta walidacja pól.
    *   Biblioteka kilku predefiniowanych postaci (domena publiczna/bajki) z awatarami.
    *   Interfejs do tworzenia sceny: wybór 2-3 postaci (własnych lub z biblioteki), zadanie tematu/opisu sceny.
    *   Wybór modelu LLM (przez OpenRouter) dla każdej postaci w scenie (lista: Gemini Flash, Claude Sonnet, DeepSeek V3, Llama 70B, o3 Min).
    *   Automatyczne generowanie dialogu tura po turze (round-robin, max 50 tur) inicjowane przyciskiem. Brak możliwości zatrzymania.
    *   Wizualizacja dialogu za pomocą dymków czatu z nazwami i awatarami postaci.
    *   Zapisywanie wygenerowanych dialogów (do 50 na użytkownika) i ich późniejsze odczytywanie/przeglądanie.
    *   Zarządzanie profilem użytkownika: możliwość wprowadzenia i zapisania własnego klucza API OpenRouter (szyfrowanego AES-GCM w bazie).
    *   Mechanizm "specjalnego użytkownika" (nadawany ręcznie) pozwalający na użycie ograniczonego, globalnego klucza API aplikacji.
    *   Obsługa błędów API OpenRouter z odpowiednimi komunikatami. Obsługa błędów w trakcie generowania dialogu (wyświetlenie częściowego dialogu i błędu).
    *   Prosta strona startowa po zalogowaniu z linkami do kluczowych sekcji.

**b. Kluczowe historie użytkownika i ścieżki korzystania:**
    *   **Jako fan fikcji/pisarz, chcę:**
        *   Zarejestrować się i zalogować do aplikacji.
        *   Stworzyć własną, unikalną postać AI, podając jej nazwę, opis charakteru i opcjonalnie wgrywając awatar.
        *   Przeglądać listę moich postaci i edytować je lub usuwać.
        *   Wybrać 2-3 postacie (własne lub z gotowej biblioteki) do interakcji.
        *   Zdefiniować temat rozmowy lub scenerię, w której ma się ona odbywać.
        *   Wybrać model językowy (LLM) dla każdej z postaci w scenie.
        *   Rozpocząć automatyczne generowanie dialogu między wybranymi postaciami i obserwować go.
        *   Zapisać interesujący dialog, aby móc do niego wrócić później.
        *   Przeglądać listę moich zapisanych dialogów.
        *   Wprowadzić mój własny klucz API OpenRouter w ustawieniach profilu, aby korzystać z własnych limitów.
    *   **Jako administrator/specjalny użytkownik, chcę:**
        *   Korzystać z aplikacji bez konieczności podawania własnego klucza API (w ramach limitu klucza globalnego).

**c. Ważne kryteria sukcesu i sposoby ich mierzenia:**
    *   **Średni czas sesji:** Minimum 5 minut (mierzone od logowania do ostatniej interakcji lub wylogowania).
    *   **Tworzenie postaci:** 20% zarejestrowanych użytkowników tworzy przynajmniej jedną własną postać AI (mierzone przez liczbę użytkowników z co najmniej jedną własną postacią / liczba zarejestrowanych użytkowników).
    *   **Retencja:** 50% zarejestrowanych użytkowników wraca do aplikacji w ciągu tygodnia od pierwszego użycia (mierzone przez liczbę użytkowników, którzy zalogowali się ponownie w ciągu 7 dni od rejestracji / liczba zarejestrowanych użytkowników).

**d. Wszelkie nierozwiązane kwestie lub obszary wymagające dalszego wyjaśnienia:**
    *   Brak zidentyfikowanych na tym etapie. Domyślne opisy postaci startowych zostaną zdefiniowane później.

**e. Wymienione przyszłe propozycje funkcjonalności:**
    *   Możliwość zatrzymania generowania dialogu przez użytkownika.
    *   Wyszukiwanie w historii zapisanych dialogów.
    *   Moderacja treści / filtrowanie nieodpowiednich odpowiedzi LLM.
    *   Zaawansowane opcje sterowania charakterem wypowiedzi postaci (poza opisem).
    *   Wyświetlanie informacji o charakterystyce lub koszcie modeli LLM.
    *   Obsługa przekroczenia limitu kontekstu LLM przy długich dialogach.
    *   System ocen i rekomendacji interesujących zestawień postaci.
    *   Dzielenie się postaciami między użytkownikami.
    *   Udostępnianie rozegranych scen innym użytkownikom.
    *   Eksport dialogów do różnych formatów.
    *   Wsparcie dla modeli lokalnych i niestandardowych endpointów AI.
    *   Parametryzowanie postaci (np. suwaki intensywności emocji).
    *   Zaawansowane narzędzia do tworzenia osobowości (np. trenowanie na korpusach).
    *   Zaawansowana konfiguracja parametrów LLM (temperatura, top_p, max tokens).
    *   Interakcje więcej niż 3 postaci.

</prd_planning_summary>

<unresolved_issues>
[Brak]
</unresolved_issues>

<future_improvements>
1.  Możliwość zatrzymania generowania dialogu przez użytkownika.
2.  Wyszukiwanie w historii zapisanych dialogów.
3.  Moderacja treści / filtrowanie nieodpowiednich odpowiedzi LLM.
4.  Zaawansowane opcje sterowania charakterem wypowiedzi postaci (poza opisem).
5.  Wyświetlanie informacji o charakterystyce lub koszcie modeli LLM.
6.  Obsługa przekroczenia limitu kontekstu LLM przy długich dialogach.
7.  System ocen i rekomendacji interesujących zestawień postaci.
8.  Dzielenie się postaciami między użytkownikami.
9.  Udostępnianie rozegranych scen innym użytkownikom.
10. Eksport dialogów do różnych formatów.
11. Wsparcie dla modeli lokalnych i niestandardowych endpointów AI.
12. Parametryzowanie postaci (np. suwaki intensywności emocji).
13. Zaawansowane narzędzia do tworzenia osobowości (np. trenowanie na korpusach).
14. Zaawansowana konfiguracja parametrów LLM (temperatura, top_p, max tokens).
15. Interakcje więcej niż 3 postaci.
</future_improvements>
</conversation_summary>