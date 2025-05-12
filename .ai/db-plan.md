# Schemat bazy danych PostgreSQL dla d-AI-logi

Poniżej znajduje się kompleksowy plan struktury bazy danych wraz z tabelami, relacjami, indeksami oraz dodatkowymi uwagami.

---

## 1. Tabele i ich definicje

### a. Tabela `User`
- **id**: BIGINT, PRIMARY KEY  
- **name**: VARCHAR(50) NOT NULL, UNIQUE  
- **password_hash**: TEXT NOT NULL  
- **is_special_user**: BOOLEAN NOT NULL DEFAULT FALSE  
- **created_at**: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  
- **updated_at**: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  

---

### b. Tabela `LLM`
- **id**: BIGINT, PRIMARY KEY  
- **name**: VARCHAR(100) NOT NULL  
- **openrouter_identifier**: VARCHAR(100) NOT NULL  

---

### c. Tabela `Avatar`
- **id**: BIGINT, PRIMARY KEY  
- **data**: BLOB (BYTEA w PostgreSQL) NOT NULL  
- **format_type**: VARCHAR(20) NOT NULL  
- **created_at**: TIMESTAMP WITH TIME ZONE NOT NULL  
- **updated_at**: TIMESTAMP WITH TIME ZONE NOT NULL  

---

### d. Tabela `Character`
- **id**: BIGINT, PRIMARY KEY  
- **user_id**: BIGINT NOT NULL  
  - FOREIGN KEY REFERENCES `User`(id) ON DELETE CASCADE  
- **name**: VARCHAR(100) NOT NULL  
  - Unikalność w obrębie użytkownika: UNIQUE(user_id, name)  
- **description**: TEXT NOT NULL  
- **short_description**: TEXT NOT NULL  
- **avatar_id**: BIGINT  
  - FOREIGN KEY REFERENCES `Avatar`(id) ON DELETE SET NULL (reprezentuje powiązanie z awatarem)  
- **is_global**: BOOLEAN NOT NULL  
  - (domyślnie ustawiane na FALSE na poziomie aplikacji)  
- **default_llm_id**: BIGINT  
  - FOREIGN KEY REFERENCES `LLM`(id) ON DELETE SET NULL  
- **created_at**: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  
- **updated_at**: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  

---

### e. Tabela `Dialogue`
- **id**: BIGINT, PRIMARY KEY  
- **user_id**: BIGINT NOT NULL  
  - FOREIGN KEY REFERENCES `User`(id) ON DELETE CASCADE  
- **name**: VARCHAR(255) NOT NULL  
- **scene_description**: TEXT  
- **is_global**: BOOLEAN NOT NULL DEFAULT FALSE  
- **status**: VARCHAR(20) NOT NULL  
  - CHECK (status IN ('COMPLETED', 'FAILED'))  
- **created_at**: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  
- **updated_at**: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  

---

### f. Tabela `DialogueMessage`
- **id**: BIGINT, PRIMARY KEY  
- **dialogue_id**: BIGINT NOT NULL  
  - FOREIGN KEY REFERENCES `Dialogue`(id) ON DELETE CASCADE  
- **character_id**: BIGINT NOT NULL  
  - FOREIGN KEY REFERENCES `Character`(id) ON DELETE RESTRICT  
- **turn_number**: INTEGER NOT NULL  
  - Numer tury wiadomości w ramach dialogu  
- **content**: TEXT NOT NULL  
- **timestamp**: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  

---

### g. Tabela `DialogueCharacterConfig`
- **dialogue_id**: BIGINT NOT NULL  
  - FOREIGN KEY REFERENCES `Dialogue`(id) ON DELETE CASCADE  
- **character_id**: BIGINT NOT NULL  
  - FOREIGN KEY REFERENCES `Character`(id) ON DELETE RESTRICT  
- **llm_id**: BIGINT NOT NULL  
  - FOREIGN KEY REFERENCES `LLM`(id) ON DELETE RESTRICT  
- **Primary Key**: (dialogue_id, character_id)  

---

## 2. Relacje między tabelami

- `User` 1 : N `Character`  
  (Usunięcie użytkownika powoduje kaskadowe usunięcie jego rekordów w tabeli `Character`)

- `User` 1 : N `Dialogue`  
  (Usunięcie użytkownika powoduje kaskadowe usunięcie powiązanych dialogów)

- `Dialogue` 1 : N `DialogueMessage`  
  (Usunięcie dialogu powoduje kaskadowe usunięcie jego wiadomości)

- `Dialogue` 1 : N `DialogueCharacterConfig`  
  (Usunięcie dialogu powoduje kaskadowe usunięcie powiązanych konfiguracji postaci)

- `Character` 1 : N `DialogueMessage`  
  (Usunięcie postaci używanej w dialogu jest zablokowane – ograniczenie RESTRICT; logika MVP uniemożliwia usunięcie postaci, jeśli jest powiązana z dialogiem)

- `Character` 1 : N `DialogueCharacterConfig`  
  (Podobnie, usunięcie postaci używanej w konfiguracji dialogu jest RESTRICT)

- `LLM` 1 : N `DialogueCharacterConfig`  
  (Każda konfiguracja dialogu zawiera odniesienie do modelu LLM; usunięcie LLM jest zablokowane)

- `LLM` 1 : N `Character` przez kolumnę `default_llm_id`  
  (W przypadku usunięcia wpisu w tabeli `LLM`, wartość `default_llm_id` w rekordach `Character` zostanie ustawiona na NULL – ON DELETE SET NULL)

- `Avatar` 1 : 1 `Character`  
  (Relacja jeden-do-jednego; kolumna `avatar_id` w tabeli `Character` wskazuje na rekord w tabeli `Avatar`. W przypadku usunięcia awatara, `avatar_id` zostaje ustawione na NULL)

---

## 3. Indeksy i ograniczenia

- **Tabela `User`:**
  - Indeks unikalny na kolumnie `email`.

- **Tabela `Character`:**
  - Unikalność ograniczona na kombinację (`user_id`, `name`).
  - Klucz obcy `user_id` z ON DELETE CASCADE.
  - Klucz obcy `default_llm_id` z ON DELETE SET NULL.
  - Klucz obcy `avatar_id` z ON DELETE SET NULL.

- **Tabela `Dialogue`:**
  - Klucz obcy `user_id` z ON DELETE CASCADE.
  - Ograniczenie CHECK na `status` (dozwolone wartości: 'COMPLETED', 'FAILED').

- **Tabela `DialogueMessage`:**
  - Klucze obce:
    - `dialogue_id` z ON DELETE CASCADE.
    - `character_id` z ON DELETE RESTRICT.

- **Tabela `DialogueCharacterConfig`:**
  - Definicja klucza głównego jako (dialogue_id, character_id) zapobiega duplikatom.
  - Klucze obce:
    - `dialogue_id` z ON DELETE CASCADE.
    - `character_id` oraz `llm_id` z ON DELETE RESTRICT.

---

## 4. Dodatkowe uwagi i decyzje projektowe

- Proste polecenia SQL, aby rozwiązanie było niezależne od bazy danych (np. Postgress, lub H2)
- Wszystkie klucze główne używają typu `BIGINT`, co zapewnia skalowalność.  
- Wszystkie znaczniki czasu (`created_at`, `updated_at`, `timestamp`) są przechowywane w strefie UTC.  
- Awatary przechowywane są jako BLOB (`BYTEA`), a ich ładowanie można zoptymalizować (lazy loading) na poziomie aplikacji przy użyciu Hibernate.  
- Użytkownik `admin` powinien zostać utworzony podczas inicjalizacji bazy – jest wykorzystywany do przypisania globalnych postaci (`is_global = true`).  
- Logika dotycząca limitów (np. liczba postaci przypadających na użytkownika, liczba tur dialogu) oraz dodatkowe walidacje usunięć (RESTRICT dla postaci używanych w dialogach) zostaną obsłużone po stronie aplikacji.

---
