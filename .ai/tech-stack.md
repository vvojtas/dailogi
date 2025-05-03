# Tech Stack

## Frontend - Astro z React dla komponentów interaktywnych
- Astro 5 pozwala na tworzenie szybkich, wydajnych stron i aplikacji z minimalną ilością JavaScript
- React 19 zapewni interaktywność tam, gdzie jest potrzebna
- TypeScript 5 dla statycznego typowania kodu i lepszego wsparcia IDE
- Tailwind 4 pozwala na wygodne stylowanie aplikacji
- Shadcn/ui zapewnia bibliotekę dostępnych komponentów React, na których oprzemy UI

## Backend - Java 21 i Spring boot
- Spring data do integracji z bazą danych
- Spring security do mechanizmu authenticacji i authoryzacji
- Spring web do wystawienia REST API
- JUnit 5 i Mockito do testowania jednostkowego i integracyjnego
- Lombok redukuje kod szablonowy

## Baza danych
- PostgreSQL - relacyjna baza danych

## Testowanie
### Frontend
- Vitest do testów jednostkowych JS/TS
- React Testing Library (RTL) do testowania komponentów React
- Playwright/Cypress do testów End-to-End (E2E)

### Backend
- JUnit 5 jako framework testowy Java
- Mockito do mockowania zależności
- Spring Boot Test do testów integracyjnych
- H2 Database/Testcontainers do testowania bazy danych
- MockMvc/RestAssured do testowania API

### API
- Postman/Newman do manualnych i automatycznych testów API

### Analiza kodu
- SonarQube do analizy jakości kodu

## AI - Komunikacja z modelami przez usługę Openrouter.ai
- Dostęp do szerokiej gamy modeli (OpenAI, Anthropic, Google i wiele innych), które pozwolą nam znaleźć rozwiązanie zapewniające wysoką efektywność i niskie koszta
- Pozwala na ustawianie limitów finansowych na klucze API

## CI/CD i Hosting
- Github Actions do tworzenia pipeline'ów CI/CD
- Konteneryzacja aplikacji za pomocą Docker 