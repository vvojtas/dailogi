package com.github.vvojtas.dailogi_server.model.dialogue.response; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import com.github.vvojtas.dailogi_server.model.character.response.DialogueCharacterSummaryDTO; 
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime; 
import java.util.List; 
 
/** 
 * Summary information about a dialogue for list views 
 */ 
@Schema(description = "Summary information about a dialogue, suitable for list views")
public record DialogueSummaryDTO( 
    @Schema(description = "Unique identifier for the dialogue", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id") Long id, 

    @Schema(description = "Name or title of the dialogue", example = "AI Debate", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name") String name, 

    @Schema(description = "Description of the scene or context", example = "Debate in a coffee shop", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("scene_description") String sceneDescription, 

    @Schema(description = "Current status of the dialogue", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("status") String status, 

    @Schema(description = "Timestamp when the dialogue was created", example = "2023-10-26T10:15:30+00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("created_at") OffsetDateTime createdAt, 

    @Schema(description = "Timestamp when the dialogue was last updated", example = "2023-10-26T10:20:00+00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("updated_at") OffsetDateTime updatedAt, 

    @Schema(description = "Summary information of the characters participating", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("characters") List<DialogueCharacterSummaryDTO> characters 
) {} 
