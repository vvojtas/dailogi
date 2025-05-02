package com.github.vvojtas.dailogi_server.model.character.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Model for base64 encoded avatar data
 */
@Schema(description = "Base64 encoded avatar data with content type")
public record AvatarRequest(
    @Schema(description = "Base64 encoded image data", example = "iVBORw0KGgoAAAANSUhEUgAAAAEA...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Base64 encoded image data is required")
    @JsonProperty("data") String data,

    @Schema(description = "Content type of the image", example = "image/png", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Content type is required")
    @Pattern(regexp = "image/(png|jpeg)", message = "Only PNG and JPEG formats are allowed")
    @JsonProperty("content_type") String contentType
) {} 