package com.github.vvojtas.dailogi_server.controller.auth;

import com.github.vvojtas.dailogi_server.apikey.api.ApiKeyStatusQuery;
import com.github.vvojtas.dailogi_server.apikey.api.DeleteApiKeyCommand;
import com.github.vvojtas.dailogi_server.apikey.api.SetApiKeyCommand;
import com.github.vvojtas.dailogi_server.apikey.application.ApiKeyCommandService;
import com.github.vvojtas.dailogi_server.apikey.application.ApiKeyQueryService;
import com.github.vvojtas.dailogi_server.model.apikey.request.ApiKeyRequest;
import com.github.vvojtas.dailogi_server.model.apikey.response.ApiKeyResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user's OpenRouter API key
 */
@RestController
@RequestMapping("/api/users/current/api-key")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Keys", description = "Operations for managing OpenRouter API keys")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {
    
    private final ApiKeyCommandService apiKeyCommandService;
    private final ApiKeyQueryService apiKeyQueryService;
    
    /**
     * Set or update the OpenRouter API key for the current user
     * 
     * @param request the request containing the API key to set
     * @return the response with updated status
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set or update OpenRouter API key", 
               description = "Sets or updates the OpenRouter API key for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API key set successfully", 
                     content = @Content(schema = @Schema(implementation = ApiKeyResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid API key format"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiKeyResponseDTO> setApiKey(@Valid @RequestBody ApiKeyRequest request) {
        log.debug("Setting API key for current user");
        
        // Create domain command from request
        SetApiKeyCommand command = new SetApiKeyCommand(request.apiKey());
        apiKeyCommandService.setApiKey(command);
        
        return ResponseEntity.ok(new ApiKeyResponseDTO(true));
    }
    
    /**
     * Delete the OpenRouter API key for the current user
     * 
     * @return the response with updated status
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete OpenRouter API key", 
               description = "Deletes the OpenRouter API key for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API key deleted successfully", 
                     content = @Content(schema = @Schema(implementation = ApiKeyResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiKeyResponseDTO> deleteApiKey() {
        log.debug("Deleting API key for current user");
        
        // Create domain command
        DeleteApiKeyCommand command = new DeleteApiKeyCommand();
        apiKeyCommandService.deleteApiKey(command);
        
        return ResponseEntity.ok(new ApiKeyResponseDTO(false));
    }
    
    /**
     * Check if the current user has an API key set
     * 
     * @return the response with the status
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check API key status", 
               description = "Checks if the authenticated user has an OpenRouter API key set")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = ApiKeyResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiKeyResponseDTO> checkApiKeyStatus() {
        log.debug("Checking API key status for current user");
        
        // Create domain query
        ApiKeyStatusQuery query = new ApiKeyStatusQuery();
        ApiKeyResponseDTO response = apiKeyQueryService.getApiKeyStatus(query);
        
        return ResponseEntity.ok(response);
    }
} 