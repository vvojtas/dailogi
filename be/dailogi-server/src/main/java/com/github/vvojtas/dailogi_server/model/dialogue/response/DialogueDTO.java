package com.github.vvojtas.dailogi_server.model.dialogue.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.time.OffsetDateTime; 
import java.util.List; 
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueCharacterConfigDTO; 
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueMessageDTO; 
 
/** 
 * Detailed DTO for Dialogue entity with character configs and messages 
 */ 
public record DialogueDTO( 
    @JsonProperty("id") Long id, 
    @JsonProperty("name") String name, 
    @JsonProperty("scene_description") String sceneDescription, 
    @JsonProperty("status") String status, 
    @JsonProperty("created_at") OffsetDateTime createdAt, 
    @JsonProperty("updated_at") OffsetDateTime updatedAt, 
    @JsonProperty("character_configs") List<DialogueCharacterConfigDTO> characterConfigs, 
    @JsonProperty("messages") List<DialogueMessageDTO> messages 
) {} 
