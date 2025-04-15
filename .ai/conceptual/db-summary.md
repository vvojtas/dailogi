<conversation_summary>
<decisions>
1.  Klucz API OpenRouter: Będzie jeden globalny klucz, nie będzie przechowywany w bazie danych (zarządzany poza bazą).
2.  Awatary: Będą przechowywane bezpośrednio w bazie danych w tabeli `Character` jako typ BLOB (`byte[]`). Lazy loading w Hibernate będzie rozważony.
3.  Nazwy Postaci: Muszą być unikalne w obrębie jednego użytkownika (ograniczenie UNIQUE na `user_id`, `name`).
4.  Postacie Predefiniowane: Będą oznaczone flagą `is_global = true` i będą należeć do dedykowanego użytkownika `admin`. Zostaną wprowadzone przez skrypty inicjalizacyjne.
5.  Tworzenie Dialogów: Encja `Dialogue` jest tworzona w bazie dopiero po jawnym zapisaniu przez użytkownika i będzie zawierać pole `name`.
6.  Limit Tur: Egzekwowany w logice aplikacji.
7.  Limity Użytkownika (Postaci, Dialogi): Egzekwowane w logice aplikacji.
8.  Modele LLM: Będzie dedykowana tabela `LLM` (`id`, `name`, `openrouter_identifier`) zasilająca dropdowny. Dane zostaną wprowadzone przez skrypty inicjalizacyjne.
9.  Status Dialogu: Tabela `Dialogue` będzie zawierać pole `status` z wartościami 'COMPLETED' lub 'FAILED'. Pole `error_message` nie jest wymagane w MVP.
10. Kaskadowe Usuwanie: Usunięcie Użytkownika (`User`) powoduje kaskadowe usunięcie jego Postaci (`Character`) i Dialogów (`Dialogue`). W MVP, usuwanie Postaci używanej w Dialogu jest blokowane na poziomie aplikacji (`ON DELETE RESTRICT` w bazie). Soft delete nie będzie stosowany.
11. Nazwa Encji Tury: Używana będzie nazwa `DialogueMessage` zamiast `DialogueTurn`.
12. Usuwanie Postaci a Widok Dialogu: W MVP usuwanie jest blokowane. W przyszłości, jeśli postać zostanie usunięta, w widoku dialogu zamiast jej nazwy pojawi się "Anonim" (logika po stronie aplikacji).
13. Relacja Dialog-Postać-LLM: Zostanie użyta tabela łącząca `DialogueCharacterConfig` (`dialogue_id`, `character_id`, `llm_id`).
14. Indeksowanie BLOB: Kolumna `avatar` nie będzie indeksowana.
15. Typ ID: Klucze główne będą typu `BIGINT`.
16. Domyślny LLM dla Postaci: Tabela `Character` będzie zawierać `default_llm_id` (nullable FK do `LLM`), używane do preselekcji LLM przy dodawaniu postaci do sceny (dotyczy wszystkich postaci). `ON DELETE SET NULL` dla tej relacji.
17. Użytkownik `admin`: Będzie istniał użytkownik `admin` utworzony podczas inicjalizacji bazy, służący m.in. do posiadania globalnych postaci i potencjalnie innych uprawnień.
18. Znaczniki Czasu: Tabele `User`, `Character`, `Dialogue` będą miały kolumny `created_at` (z wartością domyślną) i `updated_at` (zarządzane przez aplikację/Hibernate). Tabela `DialogueMessage` będzie miała kolumnę `timestamp` (z wartością domyślną).
19. Strefa czasowa: Wszystkie znaczniki czasowe są w strefie UTC
</decisions>

<matched_recommendations>
1.  **Encje Końcowe:** Zdefiniowano strukturę tabel `User`, `LLM`, `Character`, `Dialogue`, `DialogueMessage`.
2.  **Tabela Łącząca Końcowa:** Zdefiniowano strukturę tabeli `DialogueCharacterConfig` z kluczem złożonym i odpowiednimi kluczami obcymi.
3.  **Relacje i Kaskady Końcowe:** Określono typy relacji i zachowanie kaskadowe (CASCADE, RESTRICT, SET NULL) dla wszystkich kluczowych powiązań.
4.  **Typy Danych i Ograniczenia:** Wybrano standardowe typy danych (`BIGINT`, `VARCHAR`, `TEXT`, `BLOB`/`byte[]`, `BOOLEAN`, `TIMESTAMP`, `INTEGER`) oraz zdefiniowano ograniczenia (`UNIQUE`, `NOT NULL`, `CHECK`, `DEFAULT`).
5.  **Awatar:** Potwierdzono przechowywanie jako BLOB w `Character` z sugestią użycia lazy loading.
6.  **Domyślny LLM:** Dodano kolumnę `default_llm_id` do `Character`.
7.  **Znaczniki Czasu:** Dodano odpowiednie kolumny znaczników czasu do tabel.
8.  **Indeksowanie:** Określono kolumny do indeksowania (FK, email, unikalne kombinacje) i wykluczono kolumnę BLOB.
9.  **Logika Aplikacji:** Zidentyfikowano logikę, która musi być zaimplementowana w warstwie aplikacji (limity, blokady usuwania, zarządzanie `updated_at`).
10. **Inicjalizacja Danych:** Zidentyfikowano potrzebę skryptów inicjalizacyjnych dla użytkownika `admin`, danych `LLM` i globalnych postaci.
</matched_recommendations>

<database_planning_summary>
**a. Główne wymagania dotyczące schematu bazy danych:**
Schemat bazy danych ma wspierać podstawowe funkcjonalności aplikacji d-AI-logi w wersji MVP. Obejmuje to zarządzanie użytkownikami (w tym specjalnym typem użytkownika), tworzenie i zarządzanie postaciami AI (własnymi i globalnymi), definiowanie modeli LLM, tworzenie, generowanie i zapisywanie dialogów między postaciami, wraz z konfiguracją użytych LLM dla każdej postaci w danym dialogu. Schemat ma być oparty na PostgreSQL, ale z zachowaniem agnostycyzmu SQL poprzez podejście code-first z Hibernate. Kluczowe jest rozróżnienie między postaciami globalnymi a użytkownika, obsługa limitów (w aplikacji) oraz specyficzne zasady usuwania danych. Awatary postaci będą przechowywane jako BLOB.

**b. Kluczowe encje i ich relacje:**
*   **User:** Przechowuje dane użytkownika (email, hash hasła, flaga specjalnego użytkownika, znaczniki czasu). Jest właścicielem Postaci i Dialogów.
*   **LLM:** Słownik dostępnych modeli językowych (`id`, `name`, `openrouter_identifier`).
*   **Character:** Reprezentuje postać AI (nazwa, opisy, awatar BLOB, flaga globalna, domyślny LLM, znaczniki czasu). Należy do `User`. Ma ograniczenie unikalności na (`user_id`, `name`). Może mieć domyślny `LLM`.
*   **Dialogue:** Reprezentuje zapisany dialog (nazwa, opis sceny, status 'COMPLETED'/'FAILED', znaczniki czasu). Należy do `User`.
*   **DialogueMessage:** Pojedyncza tura/wiadomość w dialogu (treść, numer tury, znacznik czasu). Należy do `Dialogue` i wskazuje na `Character` mówiącego.
*   **DialogueCharacterConfig:** Tabela łącząca `Dialogue` i `Character`, przechowująca dodatkowo `LLM`, który został użyty przez daną postać w tym konkretnym dialogu. Klucz główny złożony z (`dialogue_id`, `character_id`).

**Relacje:**
*   `User` 1:N `Character` (Cascade Delete)
*   `User` 1:N `Dialogue` (Cascade Delete)
*   `Dialogue` 1:N `DialogueMessage` (Cascade Delete)
*   `Dialogue` 1:N `DialogueCharacterConfig` (Cascade Delete)
*   `Character` 1:N `DialogueMessage` (Restrict Delete - w aplikacji dla MVP)
*   `Character` 1:N `DialogueCharacterConfig` (Restrict Delete - w aplikacji dla MVP)
*   `LLM` 1:N `DialogueCharacterConfig` (Restrict Delete)
*   `LLM` 1:N `Character` (`default_llm_id`) (Set Null on Delete)

**c. Ważne kwestie dotyczące bezpieczeństwa i skalowalności:**
*   **Bezpieczeństwo:** Hasła użytkowników są hashowane. Klucz API OpenRouter nie jest przechowywany w bazie. Uprawnienia użytkownika `admin` i `is_special_user` będą zarządzane. Zasady usuwania danych (`CASCADE`, `RESTRICT`) są zdefiniowane.
*   **Skalowalność/Wydajność:** Przechowywanie awatarów jako BLOB może wpłynąć na wydajność zapytań listujących postacie i rozmiar bazy – rozważenie lazy loading jest zalecane. Indeksowanie kluczy obcych i często używanych kolumn jest zaplanowane. Limity (postaci, dialogów, tur) są obsługiwane w aplikacji, co daje większą elastyczność. Typ danych `BIGINT` dla ID zapewnia przestrzeń na przyszły wzrost.

**d. Wszelkie nierozwiązane kwestie lub obszary wymagające dalszego wyjaśnienia:**
*   Chociaż lazy loading dla awatarów został wspomniany jako sugestia, ostateczna decyzja i implementacja tej optymalizacji pozostaje do wykonania na etapie kodowania encji Hibernate.
*   Dokładny mechanizm zarządzania użytkownikiem `admin` i jego uprawnieniami (poza byciem właścicielem globalnych postaci) wymaga dalszego zdefiniowania w logice aplikacji.
</database_planning_summary>

<unresolved_issues>
[Brak krytycznych nierozwiązanych kwestii dotyczących projektu schematu bazy danych na tym etapie. Dalsze szczegóły implementacyjne (np. lazy loading, dokładne zarządzanie `updated_at` przez Hibernate) zostaną określone podczas kodowania.]
</unresolved_issues>
</conversation_summary>