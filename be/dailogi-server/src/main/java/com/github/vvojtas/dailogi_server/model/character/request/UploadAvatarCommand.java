package com.github.vvojtas.dailogi_server.model.character.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command for uploading character avatar
 */
public record UploadAvatarCommand(
    @NotNull(message = "Avatar file is required")
    MultipartFile file
) {} 