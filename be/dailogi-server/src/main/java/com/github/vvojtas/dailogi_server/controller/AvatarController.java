package com.github.vvojtas.dailogi_server.controller;

import com.github.vvojtas.dailogi_server.avatar.api.DeleteAvatarCommand;
import com.github.vvojtas.dailogi_server.avatar.api.GetAvatarQuery;
import com.github.vvojtas.dailogi_server.avatar.api.UploadAvatarCommand;
import com.github.vvojtas.dailogi_server.avatar.application.AvatarCommandService;
import com.github.vvojtas.dailogi_server.avatar.application.AvatarQueryService;
import com.github.vvojtas.dailogi_server.model.avatar.AvatarData;
import com.github.vvojtas.dailogi_server.model.avatar.response.CharacterAvatarResponseDTO;
import com.github.vvojtas.dailogi_server.model.common.response.ErrorResponseDTO;
import com.github.vvojtas.dailogi_server.service.util.UrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Avatars", description = "Endpoints for managing character avatars")
public class AvatarController {

    private final AvatarQueryService avatarQueryService;
    private final AvatarCommandService avatarCommandService;

    @Operation(
        summary = "Get character avatar",
        description = "Retrieves the avatar image for a specific character. Authentication is optional; unauthenticated users can access avatars for global characters."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Avatar retrieved successfully",
        content = {
            @Content(mediaType = MediaType.IMAGE_PNG_VALUE),
            @Content(mediaType = MediaType.IMAGE_JPEG_VALUE)
        }
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - character is not global and user is not authenticated or does not own the character",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Character not found or character has no avatar",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @GetMapping(value = "/api/characters/{characterId}/avatar")
    public ResponseEntity<byte[]> getAvatar(
        @Parameter(
            description = "ID of the character whose avatar to retrieve",
            example = "1"
        )
        @PathVariable Long characterId,
        Authentication authentication // Authentication can be null for unauthenticated requests
    ) {
        GetAvatarQuery query = new GetAvatarQuery(characterId, authentication);
        AvatarData avatarData = avatarQueryService.getAvatarData(query);
        
        MediaType contentType = MediaType.parseMediaType(avatarData.formatType());
        
        return ResponseEntity
            .ok()
            .contentType(contentType)
            .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
            .eTag(String.valueOf(avatarData.data().hashCode())) // Using hash of data as ETag
            .body(avatarData.data());
    }
    
    @Operation(
        summary = "Upload or update character avatar",
        description = "Uploads or replaces the avatar for a character. The character must be owned by the current user. " +
                     "Supports PNG and JPEG files up to 1MB and max 256x256 pixels. Requires authentication."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Avatar uploaded successfully",
        content = @Content(
            mediaType = "application/json", 
            schema = @Schema(implementation = CharacterAvatarResponseDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid file format, size exceeds 1MB, or dimensions exceed 256x256 pixels",
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
    @PostMapping(value = "/api/characters/{characterId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CharacterAvatarResponseDTO> uploadAvatar(
        @Parameter(
            description = "ID of the character to update avatar for. Must be a character owned by the current user.",
            example = "1"
        )
        @PathVariable Long characterId,
        
        @Parameter(
            description = "Avatar image file. Supports PNG and JPEG formats up to 1MB and max 256x256 pixels.",
            required = true
        )
        @Valid @ModelAttribute UploadAvatarCommand command
    ) throws IOException {
        avatarCommandService.uploadOrUpdateAvatar(characterId, command);
        
        // Generate avatar URL using Spring HATEOAS
        String fullUri = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(AvatarController.class)
                .getAvatar(characterId, null))
            .toUri().toString();
            
        // Extract just the path and query parts to make it relative
        String avatarUrl = UrlUtil.toRelativeUri(fullUri);
        
        // Build and return the response DTO
        return ResponseEntity.ok(new CharacterAvatarResponseDTO(
            characterId,
            true, // We know it has an avatar now
            avatarUrl
        ));
    }

    @Operation(
        summary = "Delete character avatar",
        description = "Deletes the avatar for a character. The character must be owned by the current user. Requires authentication."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Avatar deleted successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - user not authenticated",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - user does not own this character",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Character not found or character has no avatar",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @DeleteMapping(value = "/api/characters/{characterId}/avatar")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> deleteAvatar(
        @Parameter(
            description = "ID of the character whose avatar to delete",
            example = "1"
        )
        @PathVariable Long characterId,
        Authentication authentication
    ) {
        DeleteAvatarCommand command = new DeleteAvatarCommand(characterId, authentication);
        avatarCommandService.deleteAvatar(command);
        return ResponseEntity.ok("Avatar deleted successfully");
    }
} 