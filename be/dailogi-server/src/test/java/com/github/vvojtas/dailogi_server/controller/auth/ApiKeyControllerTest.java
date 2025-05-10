package com.github.vvojtas.dailogi_server.controller.auth;

import com.github.vvojtas.dailogi_server.apikey.api.ApiKeyStatusQuery;
import com.github.vvojtas.dailogi_server.apikey.api.DeleteApiKeyCommand;
import com.github.vvojtas.dailogi_server.apikey.api.SetApiKeyCommand;
import com.github.vvojtas.dailogi_server.apikey.application.ApiKeyCommandService;
import com.github.vvojtas.dailogi_server.apikey.application.ApiKeyQueryService;
import com.github.vvojtas.dailogi_server.model.apikey.response.ApiKeyResponseDTO;
import com.github.vvojtas.dailogi_server.service.auth.JwtTokenProvider;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiKeyController.class)
@ActiveProfiles("test")
class ApiKeyControllerTest {

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
    private ApiKeyCommandService apiKeyCommandService;
    
    @MockitoBean
    private ApiKeyQueryService apiKeyQueryService;

    @Test
    @DisplayName("Should set API key when authenticated")
    @WithMockUser
    void shouldSetApiKey() throws Exception {
        // Arrange
        doNothing().when(apiKeyCommandService).setApiKey(any(SetApiKeyCommand.class));
        
        // Act & Assert
        mockMvc.perform(put("/api/users/current/api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"api_key\": \"test-api-key\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.has_api_key").value(true));
        
        verify(apiKeyCommandService).setApiKey(any(SetApiKeyCommand.class));
    }

    @Test
    @DisplayName("Should delete API key when authenticated")
    @WithMockUser
    void shouldDeleteApiKey() throws Exception {
        // Arrange
        doNothing().when(apiKeyCommandService).deleteApiKey(any(DeleteApiKeyCommand.class));
        
        // Act & Assert
        mockMvc.perform(delete("/api/users/current/api-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.has_api_key").value(false));
        
        verify(apiKeyCommandService).deleteApiKey(any(DeleteApiKeyCommand.class));
    }

    @Test
    @DisplayName("Should check API key status when authenticated")
    @WithMockUser
    void shouldCheckApiKeyStatus() throws Exception {
        // Arrange
        ApiKeyResponseDTO responseDTO = new ApiKeyResponseDTO(true);
        when(apiKeyQueryService.getApiKeyStatus(any(ApiKeyStatusQuery.class))).thenReturn(responseDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/users/current/api-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.has_api_key").value(true));
        
        verify(apiKeyQueryService).getApiKeyStatus(any(ApiKeyStatusQuery.class));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when setting API key without authentication")
    void shouldReturnUnauthorizedWhenSettingApiKeyWithoutAuth() throws Exception {
        mockMvc.perform(put("/api/users/current/api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"api_key\": \"test-api-key\"}"))
                .andExpect(status().isUnauthorized());
        
        verify(apiKeyCommandService, never()).setApiKey(any(SetApiKeyCommand.class));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when deleting API key without authentication")
    void shouldReturnUnauthorizedWhenDeletingApiKeyWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/users/current/api-key"))
                .andExpect(status().isUnauthorized());
        
        verify(apiKeyCommandService, never()).deleteApiKey(any(DeleteApiKeyCommand.class));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when checking API key status without authentication")
    void shouldReturnUnauthorizedWhenCheckingApiKeyStatusWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/current/api-key"))
                .andExpect(status().isUnauthorized());
        
        verify(apiKeyQueryService, never()).getApiKeyStatus(any(ApiKeyStatusQuery.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when setting empty API key")
    @WithMockUser
    void shouldReturnBadRequestWhenSettingEmptyApiKey() throws Exception {
        mockMvc.perform(put("/api/users/current/api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"api_key\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
        
        verify(apiKeyCommandService, never()).setApiKey(any(SetApiKeyCommand.class));
    }
} 