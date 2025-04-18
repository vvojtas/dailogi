package com.github.vvojtas.dailogi_server.controller;

import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.common.response.ErrorResponseDTO;
import com.github.vvojtas.dailogi_server.service.CharacterService;
import com.github.vvojtas.dailogi_server.model.character.request.CreateCharacterCommand;
import com.github.vvojtas.dailogi_server.model.character.request.UpdateCharacterCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@Validated
@Tag(name = "Characters", description = "Endpoints for managing characters in the system")
@SecurityRequirement(name = "bearer-jwt")
public class CharacterController {

    private static final int MAX_PAGE_SIZE = 50;
    private final CharacterService characterService;

    @Operation(
        summary = "Get paginated list of characters",
        description = """
            Retrieves a paginated list of characters available to the current user.
            The list includes user's personal characters and optionally global characters.
            Results are sorted with personal characters first, then global characters, both groups sorted by name.
            Requires authentication.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved characters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CharacterListDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema =@Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - user not authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CharacterListDTO> getCharacters(
        @Parameter(
            description = "Flag indicating whether to include global characters in the results",
            example = "true"
        )
        @RequestParam(defaultValue = "true") boolean includeGlobal,
        
        @Parameter(
            description = "Page number (0-based). Must be non-negative.",
            example = "0"
        )
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be non-negative") int page,
        
        @Parameter(
            description = "Number of items per page. Must be between 1 and 50.",
            example = "20"
        )
        @RequestParam(defaultValue = "20") 
        @Min(value = 1, message = "Page size must be greater than 0")
        @Max(value = MAX_PAGE_SIZE, message = "Page size must not exceed " + MAX_PAGE_SIZE) 
        int size
    ) {
        CharacterListDTO result = characterService.getCharacters(includeGlobal, PageRequest.of(page, size));
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.totalElements()));
        
        // Add pagination links
        if (page > 0) {
            headers.add("Link", buildPageLink(page - 1, size, includeGlobal, "prev"));
        }
        if (page < result.totalPages() - 1) {
            headers.add("Link", buildPageLink(page + 1, size, includeGlobal, "next"));
        }
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(result);
    }
    
    private String buildPageLink(int page, int size, boolean includeGlobal, String rel) {
        return String.format("</api/characters?page=%d&size=%d&include_global=%b>; rel=\"%s\"",
            page, size, includeGlobal, rel);
    }

    @Operation(
        summary = "Get character by ID",
        description = """
            Retrieves a single character by its ID.
            The character must either be owned by the current user or be a global character.
            Requires authentication.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved character",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CharacterDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - user not authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have access to this character",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Character not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CharacterDTO> getCharacter(
        @Parameter(
            description = "ID of the character to retrieve",
            example = "1"
        )
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(characterService.getCharacter(id));
    }

    @Operation(
        summary = "Create a new character",
        description = """
            Creates a new character for the current user.
            The character name must be unique for the user.
            Requires authentication.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Character created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CharacterDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - user not authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Character with the same name already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Character limit reached",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CharacterDTO> createCharacter(
        @Valid @RequestBody CreateCharacterCommand command
    ) {
        CharacterDTO character = characterService.createCharacter(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(character);
    }

    @Operation(
        summary = "Update an existing character",
        description = """
            Updates an existing character with new data.
            The character must be owned by the current user.
            The character name must remain unique for the user.
            Requires authentication.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Character updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CharacterDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - user not authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not own this character",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Character not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Character with the same name already exists for this user",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CharacterDTO> updateCharacter(
        @Parameter(
            description = "ID of the character to update. Must be a character owned by the current user.",
            example = "1"
        )
        @PathVariable Long id,
        
        @Valid @RequestBody UpdateCharacterCommand command
    ) {
        return ResponseEntity.ok(characterService.updateCharacter(id, command));
    }
}