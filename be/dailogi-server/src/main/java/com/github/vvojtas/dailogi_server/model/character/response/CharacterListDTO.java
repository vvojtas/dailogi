package com.github.vvojtas.dailogi_server.model.character.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List; 
 
/** 
 * DTO for paginated list of characters 
 */ 
@Schema(description = "Paginated list of characters")
public record CharacterListDTO( 
    @Schema(description = "List of characters on the current page", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("content") List<CharacterDTO> content, 

    @Schema(description = "Current page number (0-indexed)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("page") int page, 

    @Schema(description = "Number of characters per page", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("size") int size, 

    @Schema(description = "Total number of characters across all pages", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("total_elements") long totalElements, 

    @Schema(description = "Total number of pages", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("total_pages") int totalPages 
) {} 
