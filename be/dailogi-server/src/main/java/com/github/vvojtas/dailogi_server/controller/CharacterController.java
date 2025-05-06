package com.github.vvojtas.dailogi_server.controller;

import com.github.vvojtas.dailogi_server.character.api.CharacterQuery;
import com.github.vvojtas.dailogi_server.character.api.CreateCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.DeleteCharacterCommand;
import com.github.vvojtas.dailogi_server.character.api.UpdateCharacterCommand;
import com.github.vvojtas.dailogi_server.character.application.CharacterCommandService;
import com.github.vvojtas.dailogi_server.character.application.CharacterQueryService;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterListDTO;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.common.response.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.vvojtas.dailogi_server.service.util.UrlUtil;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@Validated
@Tag(name = "Characters", description = "Endpoints for managing characters in the system")
public class CharacterController {

    private static final int MAX_PAGE_SIZE = 50;
    private final CharacterQueryService characterQueryService;
    private final CharacterCommandService characterCommandService;

    @Operation(
        summary = "Get paginated list of characters",
        description = """
            Retrieves a paginated list of characters.
            If the user is authenticated, the list includes the user's personal characters and optionally global characters.
            If the user is not authenticated, only global characters are returned (if `includeGlobal` is true).
            Results are sorted with personal characters first (if applicable), then global characters, both groups sorted by name.
            Authentication is optional.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved characters",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CharacterListDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters",
        content = @Content(
            mediaType = "application/json",
            schema =@Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - user not authenticated",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @GetMapping
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
        int size,
        
        Authentication authentication
    ) {
        CharacterQuery query = new CharacterQuery(includeGlobal, PageRequest.of(page, size), authentication);
        CharacterListDTO result = characterQueryService.getCharacters(query);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.totalElements()));
        
        // Add pagination links using Spring HATEOAS
        if (result.totalPages() > 1) {
            // Add "first" link if not on first page
            if (page > 0) {
                headers.add("Link", buildPageLink(0, size, includeGlobal, "first"));
                headers.add("Link", buildPageLink(page - 1, size, includeGlobal, "prev"));
            }
            
            // Add "next" and "last" links if not on last page
            if (page < result.totalPages() - 1) {
                headers.add("Link", buildPageLink(page + 1, size, includeGlobal, "next"));
                headers.add("Link", buildPageLink(result.totalPages() - 1, size, includeGlobal, "last"));
            }
        }
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(result);
    }
    
    /**
     * Builds a properly formatted pagination link using Spring HATEOAS.
     * 
     * @param page The target page number
     * @param size The page size
     * @param includeGlobal Whether to include global characters
     * @param rel The link relation ("prev", "next", "first", "last")
     * @return A formatted link string
     */
    private String buildPageLink(int page, int size, boolean includeGlobal, String rel) {
        // Get the full URI from WebMvcLinkBuilder
        String fullUri = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(CharacterController.class)
                .getCharacters(includeGlobal, page, size, null))
            .toUri().toString();
        
        // Extract just the path and query parts to make it relative
        String relativeUri = UrlUtil.toRelativeUri(fullUri);
        
        return String.format("<%s>; rel=\"%s\"", relativeUri, rel);
    }

    @Operation(
        summary = "Get character by ID",
        description = """
            Retrieves a single character by its ID.
            If the user is authenticated, the character must either be owned by the current user or be a global character.
            If the user is not authenticated, only global characters can be retrieved.
            Authentication is optional.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved character",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CharacterDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - user not authenticated",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - user does not have access to this character",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Character not found",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @GetMapping("/{id}")
    public ResponseEntity<CharacterDTO> getCharacter(
        @Parameter(
            description = "ID of the character to retrieve",
            example = "1"
        )
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(characterQueryService.getCharacter(id));
    }

    @Operation(
        summary = "Create a new character",
        description = """
            Creates a new character for the current user.
            The character name must be unique for the user.
            Optionally can include base64-encoded avatar data during character creation.
            Supports PNG and JPEG avatar formats up to 1MB and max 256x256 pixels.
            Requires authentication.
            """
    )
    @ApiResponse(
        responseCode = "201",
        description = "Character created successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CharacterDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters or invalid avatar data",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - user not authenticated",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "409",
        description = "Character with the same name already exists",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "422",
        description = "Character limit reached",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CharacterDTO> createCharacter(
        @Valid @RequestBody com.github.vvojtas.dailogi_server.model.character.request.CreateCharacterCommand requestCommand
    ) {
        CreateCharacterCommand command = new CreateCharacterCommand(
            requestCommand.name(),
            requestCommand.shortDescription(),
            requestCommand.description(),
            requestCommand.defaultLlmId(),
            requestCommand.avatar()
        );
        CharacterDTO character = characterCommandService.createCharacter(command);
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
    @ApiResponse(
        responseCode = "200",
        description = "Character updated successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CharacterDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - user not authenticated",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - user does not own this character",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Character not found",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "409",
        description = "Character with the same name already exists for this user",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CharacterDTO> updateCharacter(
        @Parameter(
            description = "ID of the character to update. Must be a character owned by the current user.",
            example = "1"
        )
        @PathVariable Long id,
        
        @Valid @RequestBody com.github.vvojtas.dailogi_server.model.character.request.UpdateCharacterCommand requestCommand
    ) {
        UpdateCharacterCommand command = new UpdateCharacterCommand(
            id,
            requestCommand.name(),
            requestCommand.shortDescription(),
            requestCommand.description(),
            requestCommand.defaultLlmId(),
            null // Avatar not supported in update via controller yet
        );
        CharacterDTO character = characterCommandService.updateCharacter(command);
        return ResponseEntity.ok(character);
    }

    @Operation(
        summary = "Delete a character",
        description = """
            Deletes a character owned by the current user.
            The character must not be used in any dialogues.
            Requires authentication.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "Character deleted successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = String.class)
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - user not authenticated",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - user does not own this character",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Character not found",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "409",
        description = "Character is used in dialogues",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> deleteCharacter(
        @Parameter(
            description = "ID of the character to delete. Must be a character owned by the current user.",
            example = "1"
        )
        @PathVariable Long id
    ) {
        DeleteCharacterCommand command = new DeleteCharacterCommand(id);
        characterCommandService.deleteCharacter(command);
        return ResponseEntity.ok("Character successfully deleted");
    }
}