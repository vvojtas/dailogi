package com.github.vvojtas.dailogi_server.model.avatar.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command for uploading character avatar
 */
@Schema(description = "Command for uploading a character's avatar image")
public record UploadAvatarCommand(
    @Schema(
        description = "PNG image file to use as avatar. Must be exactly 256x256 pixels and not exceed 1MB.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        type = "string",
        format = "binary"
    )
    @NotNull(message = "Avatar file is required")
    MultipartFile file
) {} 