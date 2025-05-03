package com.github.vvojtas.dailogi_server.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

//import com.github.vvojtas.dailogi_server.service.ExampleService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Example controller test using @WebMvcTest to test the web layer
 * without starting the full Spring application context
 */
//@WebMvcTest(ExampleController.class)
@ActiveProfiles("test") // Use application-test.yml configuration
@Disabled
class ExampleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExampleService exampleService;

    @Test
    @DisplayName("Should return 200 OK with the processed result")
    void shouldReturnProcessedResult() throws Exception {
        // Arrange
        when(exampleService.processInput(anyString())).thenReturn("PROCESSED");

        // Act & Assert
        mockMvc.perform(get("/api/example/process")
                .param("input", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("PROCESSED"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when input parameter is missing")
    void shouldReturnBadRequestWhenInputParameterIsMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/example/process")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * This is a placeholder class for the example test
     */
    private static class ExampleController {
        // This would be the actual controller implementation
    }

    /**
     * This is a placeholder interface for the example test
     */
    private interface ExampleService {
        String processInput(String input);
    }
} 