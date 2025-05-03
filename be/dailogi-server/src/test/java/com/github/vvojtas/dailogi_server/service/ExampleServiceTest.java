package com.github.vvojtas.dailogi_server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Example service test to demonstrate testing patterns
 */
@ExtendWith(MockitoExtension.class)
@Disabled
class ExampleServiceTest {

    // Example of a service dependency to mock
    @Mock
    private SomeOtherService dependencyService;

    // Service under test
    @InjectMocks
    private ExampleService exampleService;

    @Test
    @DisplayName("Should return expected result when valid input is provided")
    void shouldReturnExpectedResultWhenValidInputIsProvided() {
        // Arrange
        String input = "test";
        String expected = "TEST";
        when(dependencyService.processInput(input)).thenReturn(expected);

        // Act
        String result = exampleService.processInput(input);

        // Assert
        assertEquals(expected, result);
        verify(dependencyService).processInput(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("Should throw exception when empty or blank input is provided")
    void shouldThrowExceptionWhenEmptyOrBlankInputIsProvided(String input) {
        // Arrange, Act, Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exampleService.processInput(input)
        );
        
        assertEquals("Input cannot be empty or blank", exception.getMessage());
    }

    /**
     * This is a placeholder class just for the example test
     */
    private static class ExampleService {
        private final SomeOtherService dependencyService;

        ExampleService(SomeOtherService dependencyService) {
            this.dependencyService = dependencyService;
        }

        String processInput(String input) {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Input cannot be empty or blank");
            }
            return dependencyService.processInput(input);
        }
    }

    /**
     * This is a placeholder interface just for the example test
     */
    private interface SomeOtherService {
        String processInput(String input);
    }
} 