package com.github.vvojtas.dailogi_server.model.dialogue.request; 
 
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
import com.github.vvojtas.dailogi_server.model.dialogue.request.DialogueCharacterConfigCommand; 
 
/** 
 * Command model for generating a dialogue 
 */ 
public record GenerateDialogueCommand( 
    @JsonProperty("scene_description") String sceneDescription, 
    @JsonProperty("character_configs") List<DialogueCharacterConfigCommand> characterConfigs 
) {} 
