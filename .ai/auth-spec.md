# Specyfikacja techniczna modułu rejestracji i logowania (Spring Security + JWT)

Poniższy dokument opisuje kluczowe zmiany i komponenty niezbędne do wdrożenia funkcjonalności rejestracji i logowania użytkowników zgodnie z wymaganiami określonymi w PRD (@prd.md) oraz technologiami opisanymi w @tech-stack.md.

## 1. Zmiany w schemacie bazy danych

Brak konieczności modyfikacji schematu `AppUser` na potrzeby rejestracji (kolumna `email` tymczasowo nieużywana). Kolumny `name`, `password_hash`, `is_special_user` oraz znaczniki czasu pozostają bez zmian.

## 2. Endpointy REST API

W pakiecie `controller/auth`:

1. **POST** `/api/auth/register`
   - RequestBody: `RegistrationRequest` (record)
     - `String name`
     - `String password`
     - `String passwordConfirmation`
   - Response: `ApiResponse` / `UserDto`
   -  Walidacja na poziomie Bean Validation (`@NotBlank`, `@Size`).
   - Sprawdzenie unikalności `name`.

2. **POST** `/api/auth/login`
   - RequestBody: `LoginRequest` (record)
     - `String name`
     - `String password`
   - Response: `JwtResponse` (record)
     - `String accessToken`
     - `String tokenType = "Bearer"`
     - `Long expiresIn`
     - `UserDto user`

3. **GET** `/api/auth/me` (opcjonalnie)
   - Zwraca `UserDto` aktualnie zalogowanego użytkownika.
   - Endpoint chroniony JWT.

4. **(Opcjonalnie)** **POST** `/api/auth/refresh`
   - RequestBody: `TokenRefreshRequest`
   - Response: `TokenRefreshResponse`
   - Mechanizm odświeżania (jeśli przewidziany).

5. **(Opcjonalnie)** **POST** `/api/auth/logout`
  - Endpoint chroniony JWT.
  - Klient usuwa token po stronie frontu (opcjonalna implementacja blacklistingu na serwerze).
  - Response: `ApiResponse` potwierdzający wylogowanie.

## 3. Serwisy i komponenty domenowe

1. **AuthService** (`@Service`)
   - Metody:
     - `UserDto register(RegistrationRequest request)`
     - `JwtResponse authenticate(LoginRequest request)`
   - Logika:
     - Hashowanie hasła przy użyciu `PasswordEncoder` (BCrypt).
     - Tworzenie i zapis `AppUser` przez `AppUserRepository`.
     - Generowanie tokena JWT (`JwtTokenProvider`).
     - Obsługa wyjątków: `UsernameAlreadyExistsException`, `PasswordMismatchException`, `InvalidCredentialsException`.

2. **AppUserDetailsService** (`implements UserDetailsService`)
   - Metoda: `loadUserByUsername(String username)`
   - Ładowanie `AppUser` przez `AppUserRepository.findByName`.
   - Mapowanie na `UserDetails` ze standardowymi rolami:
     - `ROLE_USER` zawsze.
     - `ROLE_SPECIAL` gdy `isSpecialUser == true`.

3. **JwtTokenProvider** (`@Component`)
   - Generowanie tokenów:
     - `String generateToken(Authentication authentication)`
   - Walidacja tokenów:
     - `boolean validateToken(String token)`
     - `String getUsernameFromToken(String token)`
   - Wczytywanie sekretu i okresu wygaśnięcia z `application.yml`:
     ```yaml
     jwt:
       secret: ${JWT_SECRET}
       expiration-ms: 3600000
     ```

4. **DTOs** (w pakiecie `model/dto` jako `record`y):
   - `RegistrationRequest`, `LoginRequest`, `JwtResponse`, `UserDto`, `ApiResponse`, etc.

5. **GlobalExceptionHandler** (`@ControllerAdvice`)
   - Centralna obsługa wyjątków:
     - Zwracanie `ErrorDto` ze spójną strukturą:
       ```json
       {
         "timestamp": "...",
         "status": 400,
         "error": "Bad Request",
         "message": "User already exists",
         "path": "/api/auth/register"
       }
       ```

## 4. Konfiguracja Spring Security

1. **SecurityConfig** (`@Configuration`, `@EnableWebSecurity`)
   - Beans:
     - `PasswordEncoder passwordEncoder()` (BCryptPasswordEncoder)
     - `AuthenticationManager authenticationManager(AuthenticationConfiguration config)`
   - Filtry:
     - `JwtAuthenticationFilter` dla endpointu `/api/auth/login`.
     - `JwtAuthorizationFilter` dla weryfikacji tokena dla wszystkich pozostałych `/api/**`.
   - Ustawienia HTTP Security:
     ```java
     http.csrf().disable()
         .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
       .and()
         .authorizeHttpRequests()
         .requestMatchers("/api/auth/**").permitAll()
         .anyRequest().authenticated();
     ```
   - Wyłączenie sesji, korzystanie w 100% z JWT.

2. **Filtrowanie JWT**
   - Parsowanie nagłówka `Authorization: Bearer <token>`.
   - Ustawianie `SecurityContextHolder`.

## 5. Zmiany w istniejących serwisach i repozytoriach

1. **AppUserRepository** (`interface extends JpaRepository<AppUser, Long>`)
   - Dodanie metod:
     ```java
     boolean existsByName(String name);
     Optional<AppUser> findByName(String name);
     ```

2. **Modyfikacja Controllerów**
   - Iniekcja `@AuthenticationPrincipal UserDetails userDetails` lub `Principal`.
   - Endpointy operujące na danych użytkownika powinny wykorzystywać `UserDto userDto = userService.getCurrentUser();` zamiast jawnego przekazywania userId.

3. **Konfiguracja wyjątków i walidacji**
   - Bean Validation dla modeli wejściowych.
   - Dodanie nowych custom exceptions i mapperów w `GlobalExceptionHandler`.

## 6. Wsparcie logowania na stronie Swaggera

- Należy skonfigurować Swaggera (OpenAPI) tak, aby umożliwiał wprowadzenie tokena JWT poprzez przycisk "Authorize" w interfejsie użytkownika.
- Dokumentacja Swaggera powinna zawierać informacje o uzyskiwaniu tokena JWT za pomocą endpointu `/api/auth/login` oraz przykładowe wywołania z nagłówkiem `Authorization: Bearer <token>`.
- W konfiguracji Swaggera (np. w klasie `SwaggerConfig`) należy dodać ustawienia zabezpieczeń umożliwiające testowanie chronionych endpointów zgodnie z konfiguracją Spring Security.
- W dokumentacji dodać przykłady oraz objaśnienia, jak korzystać z mechanizmu autoryzacji JWT podczas testowania API.