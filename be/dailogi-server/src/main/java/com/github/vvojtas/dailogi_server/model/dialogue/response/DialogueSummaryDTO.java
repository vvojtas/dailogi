package com.github.vvojtas.dailogi_server.model.dialogue.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.time.OffsetDateTime; 
import java.util.List; 
import com.github.vvojtas.dailogi_server.model.character.response.DialogueCharacterSummaryDTO; 
 
/** 
 * Summary information about a dialogue for list views 
 */ 
public record DialogueSummaryDTO( 
    @JsonProperty("id") Long id, 
    @JsonProperty("name") String name, 
    @JsonProperty("scene_description") String sceneDescription, 
    @JsonProperty("status") String status, 
    @JsonProperty("created_at") OffsetDateTime createdAt, 
    @JsonProperty("updated_at") OffsetDateTime updatedAt, 
    @JsonProperty("characters") List<DialogueCharacterSummaryDTO> characters 
) {} 
