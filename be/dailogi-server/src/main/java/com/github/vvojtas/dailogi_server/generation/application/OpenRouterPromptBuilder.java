package com.github.vvojtas.dailogi_server.generation.application;

import com.github.vvojtas.dailogi_server.dialogue.stream.api.StreamDialogueCommand;
import com.github.vvojtas.dailogi_server.generation.api.ChatMessage;
import com.github.vvojtas.dailogi_server.model.character.response.CharacterDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.request.CharacterConfigDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueCharacterConfigDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueDTO;
import com.github.vvojtas.dailogi_server.model.dialogue.response.DialogueMessageDTO;
import com.github.vvojtas.dailogi_server.model.llm.response.LLMDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder for OpenRouter prompts. Creates system messages and formats conversations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OpenRouterPromptBuilder {

    private static final String TEMPLATE_PATH = "templates/openrouter-system.template";
    private String promptTemplate;

    /**
     * Loads the prompt template from resources
     * @return The prompt template string
     */
    private String getPromptTemplate() {
        if (promptTemplate == null) {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                promptTemplate = FileCopyUtils.copyToString(reader);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load prompt template", e);
            }
        }
        return promptTemplate;
    }

    /**
     * Builds a list of chat messages for a dialogue based on the provided command and character information.
     *
     * @param command The command containing dialogue information
     * @param activeCharacterConfig The active character configuration
     * @param characters Map of character IDs to character DTOs
     * @return A list of chat messages for the OpenRouter API
     */
    public List<ChatMessage> buildDialogueMessages(
            DialogueDTO dialogueDTO,
            DialogueCharacterConfigDTO activeCharacterConfig) {
        
        log.debug("Building dialogue messages for dialogue: {}", dialogueDTO.id());
        
        List<ChatMessage> messages = new ArrayList<>();
        
        
        // Add system message with character instructions
        messages.add(buildSystemMessage(dialogueDTO, activeCharacterConfig));
        
        messages.addAll(buildConversationMessages(dialogueDTO.messages(), activeCharacterConfig));

        return messages;
    }

    private ChatMessage buildSystemMessage(
            DialogueDTO dialogueDTO,
            DialogueCharacterConfigDTO activeCharacterConfig) {
        
        String template = getPromptTemplate();
        
        // Prepare placeholders
        String characterName = activeCharacterConfig.character().name();
        String characterDescription = activeCharacterConfig.character().shortDescription();
        
        // Build other characters string
        String otherCharacters = dialogueDTO.characterConfigs().stream()
                .filter(characterConfig -> !characterConfig.equals(activeCharacterConfig))
                .map(characterConfig -> characterConfig.character().name() + " : " + characterConfig.character().shortDescription())
                .collect(Collectors.joining(", "));
        
        // Replace placeholders in template
        String systemPrompt = template
                .replace("${character_name}", characterName)
                .replace("${character_description}", characterDescription)
                .replace("${other_characters}", otherCharacters)
                .replace("${scene_description}", dialogueDTO.sceneDescription());
        
        return new ChatMessage(ChatMessage.ROLE_SYSTEM, systemPrompt);
    }
    
    /**
     * Builds messages for an existing dialogue
     */
    public List<ChatMessage> buildConversationMessages(
            List<DialogueMessageDTO> dialogueMessages,
            DialogueCharacterConfigDTO activeCharacterConfig) {
        
        List<ChatMessage> messages = new ArrayList<>();
        
        if (dialogueMessages == null || dialogueMessages.isEmpty()) {
            return messages;
        }
        
        for (DialogueMessageDTO message : dialogueMessages) {
            String role;
            String content;
            
            if (message.characterId().equals(activeCharacterConfig.character().id())) {
                // Message from the target character
                role = ChatMessage.ROLE_ASSISTANT;
                content = message.content();
            } else {
                // Message from another character
                role = ChatMessage.ROLE_USER;
                content = activeCharacterConfig.character().name() + " powiedział \"" + message.content() + "\"";
            }
            
            messages.add(new ChatMessage(role, content));
        }
        
        return messages;
    }
} 