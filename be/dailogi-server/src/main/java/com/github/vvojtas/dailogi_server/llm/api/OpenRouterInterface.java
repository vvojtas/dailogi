package com.github.vvojtas.dailogi_server.llm.api;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Interface for services that interact with OpenRouter or similar LLM providers
 */
public interface OpenRouterInterface {

    /**
     * Generates text from the given prompt using the specified LLM model
     * 
     * @param openRouterIdentifier The identifier of the LLM model to use
     * @param prompt The prompt to generate text from
     * @param tokenConsumer Consumer for individual tokens as they are generated
     * @param completionListener Listener called when generation is complete
     * @return A unique request ID for this generation
     */
    UUID generateText(
        String openRouterIdentifier,
        String prompt,
        Consumer<String> tokenConsumer,
        Runnable completionListener
    );
    
    /**
     * Cancels an ongoing text generation request
     * 
     * @param requestId The ID of the request to cancel
     * @return true if the request was cancelled successfully, false otherwise
     */
    boolean cancelGeneration(UUID requestId);
} 