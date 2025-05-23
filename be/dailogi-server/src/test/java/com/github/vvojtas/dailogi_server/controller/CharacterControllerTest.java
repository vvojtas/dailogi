package com.github.vvojtas.dailogi_server.controller;

import com.github.vvojtas.dailogi_server.character.api.CharacterQuery;
import com.github.vvojtas.dailogi_server.character.api.CreateCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.DeleteCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.UpdateCharacterCommand;
import com.github.vvojtas.dailogi_server.character.application.CharacterCommandService;
import com.github.vvojtas.dailogi_server.character.application.CharacterQueryService;
import com.github.vvojtas.dailogi_server.model.character.mapper.CharacterDropdownMapper;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDropdownDTO;
import com.github.vvojtas.dailogi_server.service.auth.JwtTokenProvider;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CharacterController.class)
@ActiveProfiles("test")
class CharacterControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtTokenProvider jwtTokenProvider() {
            return mock(JwtTokenProvider.class);
        }
        
        @Bean
        public UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }
        
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/characters/**").permitAll()
                    .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> 
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage()))
                );
            
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CharacterQueryService characterQueryService;
    
    @MockitoBean
    private CharacterCommandService characterCommandService;
    
    @MockitoBean
    private CharacterDropdownMapper characterDropdownMapper;

    private static Stream<Arguments> invalidPaginationParameters() {
        return Stream.of(
            Arguments.of(-1, 20, "Validation failed"),
            Arguments.of(0, 0, "Validation failed"),
            Arguments.of(0, 51, "Validation failed")
        );
    }

    @Test
    @DisplayName("Should return paginated list of characters for authenticated user")
    @WithMockUser
    void shouldReturnPaginatedCharacterList() throws Exception {
        // Arrange
        CharacterDTO characterDTO = new CharacterDTO(
            1L, 
            "Test Character", 
            "Short desc",
            "Description", 
            true,
            "/api/characters/1/avatar",
            true,
            1L,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
        
        CharacterListDTO expectedResponse = new CharacterListDTO(
            List.of(characterDTO),
            0,
            20,
            1L,
            1
        );
        
        when(characterQueryService.getCharacters(any(CharacterQuery.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/characters")
                .param("includeGlobal", "true")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.total_pages").value(1))
                .andExpect(jsonPath("$.total_elements").value(1));
    }

    @ParameterizedTest
    @MethodSource("invalidPaginationParameters")
    @DisplayName("Should return 400 Bad Request for invalid pagination parameters")
    void shouldReturnBadRequestForInvalidPagination(int page, int size, String expectedError) throws Exception {
        mockMvc.perform(get("/api/characters")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedError));
    }

    @Test
    @DisplayName("Should return character by ID")
    void shouldReturnCharacterById() throws Exception {
        // Arrange
        CharacterDTO character = new CharacterDTO(
            1L,
            "Test Character",
            "Short desc",
            "Description",
            false,
            null,
            true,
            null,
            OffsetDateTime.now(),
            null
        );
        
        when(characterQueryService.getCharacter(1L)).thenReturn(character);

        // Act & Assert
        mockMvc.perform(get("/api/characters/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Character"))
                .andExpect(jsonPath("$.short_description").value("Short desc"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    @DisplayName("Should create new character when authenticated")
    @WithMockUser
    void shouldCreateCharacter() throws Exception {
        // Arrange
        CharacterDTO created = new CharacterDTO(
            1L,
            "New Character",
            "Short desc",
            "Description",
            false,
            null,
            false,
            null,
            OffsetDateTime.now(),
            null
        );
        
        when(characterCommandService.createCharacter(any(CreateCharacterCommand.class))).thenReturn(created);

        // Act & Assert
        mockMvc.perform(post("/api/characters")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "New Character",
                        "short_description": "Short desc",
                        "description": "Description"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Character"))
                .andExpect(jsonPath("$.short_description").value("Short desc"));
    }

    @Test
    @DisplayName("Should update existing character when authenticated")
    @WithMockUser
    void shouldUpdateCharacter() throws Exception {
        // Arrange
        CharacterDTO updated = new CharacterDTO(
            1L,
            "Updated Character",
            "Updated short desc",
            "Updated description",
            false,
            null,
            false,
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
        
        when(characterCommandService.updateCharacter(any(UpdateCharacterCommand.class)))
            .thenReturn(updated);

        // Act & Assert
        mockMvc.perform(put("/api/characters/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Updated Character",
                        "short_description": "Updated short desc",
                        "description": "Updated description"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Character"))
                .andExpect(jsonPath("$.short_description").value("Updated short desc"));
    }

    @Test
    @DisplayName("Should delete character when authenticated")
    @WithMockUser
    void shouldDeleteCharacter() throws Exception {
        // Arrange
        doNothing().when(characterCommandService).deleteCharacter(any(DeleteCharacterCommand.class));

        // Act & Assert
        mockMvc.perform(delete("/api/characters/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Character successfully deleted"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when creating character without authentication")
    void shouldReturnUnauthorizedWhenCreatingCharacterWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/characters")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "New Character",
                        "short_description": "Short desc",
                        "description": "Description"
                    }
                    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when creating character with invalid data")
    @WithMockUser
    void shouldReturnBadRequestWhenCreatingCharacterWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/characters")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "",
                        "short_description": "",
                        "description": ""
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return character dropdown items")
    void shouldReturnCharacterDropdownItems() throws Exception {
        // Arrange
        CharacterDTO characterDTO = new CharacterDTO(
            1L, 
            "Test Character", 
            "Short desc",
            "Description", 
            true,
            "/api/characters/1/avatar",
            true,
            1L,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
        
        CharacterListDTO characterListDTO = new CharacterListDTO(
            List.of(characterDTO),
            0,
            Integer.MAX_VALUE,
            1L,
            1
        );
        
        CharacterDropdownDTO dropdownDTO = new CharacterDropdownDTO(
            1L,
            "Test Character",
            true,
            true,
            "/api/characters/1/avatar"
        );
        
        when(characterQueryService.getCharacters(any(CharacterQuery.class)))
            .thenReturn(characterListDTO);
        
        when(characterDropdownMapper.toDropdownDTOs(List.of(characterDTO)))
            .thenReturn(List.of(dropdownDTO));

        // Act & Assert
        mockMvc.perform(get("/api/characters/dropdown"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Character"))
                .andExpect(jsonPath("$[0].is_global").value(true))
                .andExpect(jsonPath("$[0].has_avatar").value(true))
                .andExpect(jsonPath("$[0].avatar_url").value("/api/characters/1/avatar"));
    }
} 