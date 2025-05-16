package com.github.vvojtas.dailogi_server.generation.api;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Interface for integration with OpenRouter API
 */
public interface OpenRouterInterface {

    /**
     * Streams a chat completion from OpenRouter API using the chat message format
     * @param openRouterIdentifier The model identifier for OpenRouter
     * @param messages List of chat messages in the conversation
     * @param apiKey The OpenRouter API key
     * @param tokenConsumer Consumer that receives each token as it's generated
     * @param completionListener Runnable called when text generation is complete
     * @return A unique identifier for this generation request
     */
    UUID streamChat(
            String openRouterIdentifier,
            List<ChatMessage> messages,
            String apiKey,
            Consumer<String> tokenConsumer,
            Runnable completionListener);
    
    /**
     * Cancels an ongoing text generation
     * @param requestId The unique identifier for the generation request
     * @return true if the generation was cancelled, false otherwise
     */
    boolean cancelGeneration(UUID requestId);
} 