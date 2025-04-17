package com.github.vvojtas.dailogi_server.model.dialogue.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List;
 
/** 
 * DTO for paginated list of dialogues 
 */ 
public record DialogueListDTO( 
    @JsonProperty("content") List<DialogueSummaryDTO> content, 
    @JsonProperty("page") int page, 
    @JsonProperty("size") int size, 
    @JsonProperty("total_elements") long totalElements, 
    @JsonProperty("total_pages") int totalPages 
) {} 
