package com.github.vvojtas.dailogi_server.model.character.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
 
/** 
 * DTO for paginated list of characters 
 */ 
public record CharacterListDTO( 
    @JsonProperty("content") List<CharacterDTO> content, 
    @JsonProperty("page") int page, 
    @JsonProperty("size") int size, 
    @JsonProperty("total_elements") long totalElements, 
    @JsonProperty("total_pages") int totalPages 
) {} 
