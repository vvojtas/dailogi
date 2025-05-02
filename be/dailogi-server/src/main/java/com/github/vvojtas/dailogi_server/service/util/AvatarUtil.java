package com.github.vvojtas.dailogi_server.service.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Utility class for avatar validation and processing operations
 */
@Slf4j
public class AvatarUtil {

    // Constants for validation
    public static final int MAX_IMAGE_SIZE_BYTES = 1048576; // 1MB
    public static final int MAX_IMAGE_DIMENSION = 256; // 256 pixels
    public static final String CONTENT_TYPE_PNG = "image/png";
    public static final String CONTENT_TYPE_JPEG = "image/jpeg";

    /**
     * Validates avatar content type (PNG and JPEG only)
     * 
     * @param contentType The content type to validate
     * @return The validated content type
     * @throws ResponseStatusException if the content type is invalid
     */
    public static String validateAvatarContentType(String contentType) {
        if (contentType == null || (!contentType.equals(CONTENT_TYPE_PNG) && !contentType.equals(CONTENT_TYPE_JPEG))) {
            log.warn("Invalid content type '{}' for avatar. Allowed: {}, {}", contentType, CONTENT_TYPE_PNG, CONTENT_TYPE_JPEG);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only PNG and JPEG files are allowed.");
        }
        return contentType;
    }

    /**
     * Validates avatar size (max 1MB)
     * 
     * @param size The size in bytes
     * @throws ResponseStatusException if the size exceeds the limit
     */
    public static void validateAvatarSize(long size) {
        if (size > MAX_IMAGE_SIZE_BYTES) {
            log.warn("File size {} bytes exceeds maximum allowed size of 1MB", size);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must not exceed 1MB.");
        }
    }

    /**
     * Validates avatar image dimensions (max 256x256)
     * 
     * @param image The BufferedImage to validate
     * @throws ResponseStatusException if dimensions exceed the limit or image is null
     */
    public static void validateAvatarDimensions(BufferedImage image) {
        if (image == null) {
            log.warn("Failed to read image, it might be corrupted or not a valid image.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image. Could not read image data.");
        }

        if (image.getWidth() > MAX_IMAGE_DIMENSION || image.getHeight() > MAX_IMAGE_DIMENSION) {
            log.warn("Invalid image dimensions: {}x{}. Maximum allowed is {}x{}", 
                image.getWidth(), image.getHeight(), MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Image dimensions must not exceed " + MAX_IMAGE_DIMENSION + "x" + MAX_IMAGE_DIMENSION + " pixels.");
        }
    }

    /**
     * Validates uploaded MultipartFile for avatar (content type, size, dimensions)
     * 
     * @param file The MultipartFile to validate
     * @return The validated content type
     * @throws ResponseStatusException If validation fails
     * @throws IOException If the file cannot be read
     */
    public static String validateAvatarFile(MultipartFile file) throws IOException {
        // Validate file is not null or empty
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is missing or empty.");
        }
        
        // Validate content type
        String contentType = validateAvatarContentType(file.getContentType());
        
        // Validate file size
        validateAvatarSize(file.getSize());

        // Read and validate image dimensions
        BufferedImage img = ImageIO.read(file.getInputStream());
        validateAvatarDimensions(img);

        log.debug("Avatar file validation successful: Type={}, Size={} bytes, Dimensions={}x{}",
            contentType, file.getSize(), img.getWidth(), img.getHeight());

        return contentType;
    }

    /**
     * Validates and decodes base64 encoded image data
     * 
     * @param base64Data The base64 encoded string
     * @param contentType The content type of the image (e.g., "image/png")
     * @return The decoded byte array if valid
     * @throws ResponseStatusException If validation fails
     */
    public static byte[] validateAndDecodeBase64Avatar(String base64Data, String contentType) {
        // Validate content type
        validateAvatarContentType(contentType);

        // Decode base64 data
        byte[] decodedData;
        try {
            decodedData = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid base64 encoded data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid base64 encoded image data.");
        }

        // Validate size
        validateAvatarSize(decodedData.length);

        // Validate image dimensions
        try (ByteArrayInputStream bis = new ByteArrayInputStream(decodedData)) {
            BufferedImage img = ImageIO.read(bis);
            validateAvatarDimensions(img);

            log.debug("Avatar validation successful: Type={}, Size={} bytes, Dimensions={}x{}",
                contentType, decodedData.length, img.getWidth(), img.getHeight());
        } catch (IOException e) {
            log.warn("Error reading image data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error processing image data: " + e.getMessage());
        }

        return decodedData;
    }
} 