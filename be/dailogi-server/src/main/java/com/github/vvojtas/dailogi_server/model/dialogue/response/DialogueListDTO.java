package com.github.vvojtas.dailogi_server.model.dialogue.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
 
/** 
 * DTO for paginated list of dialogues 
 */ 
@Schema(description = "Paginated list of dialogues")
public record DialogueListDTO( 
    @Schema(description = "List of dialogue summaries on the current page", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("content") List<DialogueSummaryDTO> content, 

    @Schema(description = "Current page number (0-indexed)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("page") int page, 

    @Schema(description = "Number of dialogues per page", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("size") int size, 

    @Schema(description = "Total number of dialogues across all pages", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("total_elements") long totalElements, 

    @Schema(description = "Total number of pages", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("total_pages") int totalPages 
) {} 
