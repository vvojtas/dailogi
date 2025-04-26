package com.github.vvojtas.dailogi_server.model.dialogue.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime; 
import java.util.List; 
 
/** 
 * Detailed DTO for Dialogue entity with character configs and messages 
 */ 
@Schema(description = "Detailed information about a single dialogue, including characters and messages")
public record DialogueDTO( 
    @Schema(description = "Unique identifier for the dialogue", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id, 

    @Schema(description = "Name or title of the dialogue", example = "AI Debate", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name") String name, 

    @Schema(description = "Description of the scene or context", example = "Debate in a coffee shop", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("scene_description") String sceneDescription, 

    @Schema(description = "Current status of the dialogue (e.g., PENDING, RUNNING, COMPLETED, FAILED)", example = "COMPLETED", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("status") String status, 

    @Schema(description = "Timestamp when the dialogue was created", example = "2023-10-26T10:15:30+00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("created_at") OffsetDateTime createdAt, 

    @Schema(description = "Timestamp when the dialogue was last updated", example = "2023-10-26T10:20:00+00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("updated_at") OffsetDateTime updatedAt, 

    @Schema(description = "Configurations of the characters participating in the dialogue", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("character_configs") List<DialogueCharacterConfigDTO> characterConfigs, 

    @Schema(description = "List of messages exchanged in the dialogue", requiredMode = Schema.RequiredMode.REQUIRED) // Can be empty
    @JsonProperty("messages") List<DialogueMessageDTO> messages 
) {} 
