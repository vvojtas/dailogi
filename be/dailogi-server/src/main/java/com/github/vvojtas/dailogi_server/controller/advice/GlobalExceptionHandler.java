package com.github.vvojtas.dailogi_server.controller.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import com.github.vvojtas.dailogi_server.exception.AuthenticationErrorException;
import com.github.vvojtas.dailogi_server.exception.ResourceNotFoundException;
import com.github.vvojtas.dailogi_server.exception.DuplicateResourceException;
import com.github.vvojtas.dailogi_server.exception.InvalidJwtException;
import com.github.vvojtas.dailogi_server.exception.NoApiKeyException;
import com.github.vvojtas.dailogi_server.model.common.response.ErrorResponseDTO;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.github.vvojtas.dailogi_server.exception.CharacterLimitExceededException;
import com.github.vvojtas.dailogi_server.exception.CryptoException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Void> handleClientAbortException(ClientAbortException e) {
        log.debug("Client aborted the connection: {}", e.getMessage());
        // No response needed as the client has already disconnected
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Constraint validation failed. Violations listed in response. Message: {}", e.getMessage(), e);
        
        Map<String, Object> details = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> 
            details.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Validation failed",
                "VALIDATION_ERROR",
                details,
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String requiredType = Optional.ofNullable(e.getRequiredType()).map(Class::getSimpleName).orElse("N/A");
        log.warn("Method argument type mismatch. Parameter: '{}', Value: '{}', Required Type: {}. Message: {}",
                e.getName(), e.getValue(), requiredType, e.getMessage(), e);
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", e.getName());
        details.put("value", e.getValue());
        Optional.ofNullable(e.getRequiredType())
                .map(Class::getSimpleName)
                .ifPresent(type -> details.put("required_type", type));
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Invalid parameter type",
                "TYPE_MISMATCH",
                details,
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found. Type: {}. Message: {}", e.getType(), e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Resource not found",
                "RESOURCE_NOT_FOUND",
                Map.of("type", e.getType()),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied. Message: {}", e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Access denied",
                "ACCESS_DENIED",
                Map.of(),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResourceException(DuplicateResourceException e) {
        log.warn("Duplicate resource. Resource: '{}'. Message: {}", e.getResourceName(), e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                e.getMessage(),
                "RESOURCE_DUPLICATE",
                Map.of("resource", e.getResourceName()),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("Bad credentials provided. Message: {}", e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Invalid username or password",
                "INVALID_CREDENTIALS",
                Map.of(),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidJwtException(InvalidJwtException e) {
        log.warn("Invalid JWT token. Message: {}", e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Invalid JWT token",
                "INVALID_TOKEN",
                Map.of(),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(AuthenticationErrorException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationErrorException(AuthenticationErrorException e) {
        log.error("Authentication error occurred. Message: {}", e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "An error occurred during authentication",
                "AUTH_ERROR",
                Map.of(),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException e) {
        log.warn("General authentication failure. Message: {}", e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Authentication failed",
                "AUTHENTICATION_FAILED",
                Map.of(),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Method argument validation failed. Violations in response. Message: {}", e.getMessage(), e);
        
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            details.put(error.getField(), error.getDefaultMessage())
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "Validation failed",
                "VALIDATION_ERROR",
                details,
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(CharacterLimitExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleCharacterLimitExceededException(CharacterLimitExceededException e) {
        log.warn("Character limit exceeded. Limit: {}. Message: {}", e.getLimit(), e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                e.getMessage(),
                "CHARACTER_LIMIT_EXCEEDED",
                Map.of("limit", e.getLimit()),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(NoApiKeyException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoApiKeyException(NoApiKeyException e) {
        log.warn("API key required. Operation: {}. Message: {}", 
                e.getOperation(), e.getMessage(), e);
        
        Map<String, Object> details = new HashMap<>();
        details.put("operation", e.getOperation());
        
        return ResponseEntity
            .status(HttpStatus.PAYMENT_REQUIRED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                e.getMessage(),
                "API_KEY_REQUIRED",
                details,
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(CryptoException.class)
    public ResponseEntity<ErrorResponseDTO> handleCryptoException(CryptoException e) {
        log.error("Cryptography error occurred: {}", e.getMessage(), e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "A critical error occurred during a security operation. Please contact support if the issue persists.",
                "CRYPTO_OPERATION_FAILED",
                Map.of(),
                OffsetDateTime.now()
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        log.error("Unexpected error occurred. Message: {}", e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponseDTO(
                "An unexpected error occurred",
                "INTERNAL_ERROR",
                Map.of("error", e.getClass().getSimpleName()),
                OffsetDateTime.now()
            ));
    }
} 