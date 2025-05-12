package com.github.vvojtas.dailogi_server.llm.application;

import com.github.vvojtas.dailogi_server.llm.api.OpenRouterInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Mock implementation of OpenRouterInterface for testing and development
 */
@Service
@Slf4j
public class OpenRouterMock implements OpenRouterInterface {

    private static final int INITIAL_DELAY_MS = 100; 
    private static final int DEFAULT_DELAY_MS = 100; // Delay between tokens
    private static final int VARIABLE_DELAY_MS = 100; // Variable delay component
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<UUID, ScheduledFuture<?>> activeGenerations = new ConcurrentHashMap<>();
    
    private final Map<String, List<String>> predefinedResponses = Map.of(
        "default", Arrays.asList(
            "Hello! I'm happy to discuss this topic with you.",
            "That's an interesting perspective. Let me add my thoughts.",
            "I think we need to consider multiple angles on this issue.",
            "I agree with some points made earlier, but I'd like to add something."
        ),
        "claude-3-opus-20240229", Arrays.asList(
            "As an advanced AI assistant, I analyze this from multiple perspectives.",
            "I'd like to offer a nuanced view that considers historical context.",
            "Let me approach this systematically, breaking down the key factors involved.",
            "While I understand the sentiment expressed, I think we should examine the evidence more carefully."
        ),
        "gpt-4-turbo-preview", Arrays.asList(
            "From my analysis, there are several considerations worth exploring here.",
            "Building on the previous points, I'd suggest looking at this differently.",
            "Let's dig deeper into the underlying assumptions of this discussion.",
            "There's a fascinating interplay of factors we should acknowledge."
        )
    );

    @Override
    public UUID generateText(
            String openRouterIdentifier,
            String prompt,
            Consumer<String> tokenConsumer,
            Runnable completionListener) {
        
        UUID requestId = UUID.randomUUID();
        log.debug("Starting mock text generation with ID {}", requestId);
        
        // Select a response based on model identifier or fallback to default
        List<String> possibleResponses = predefinedResponses.getOrDefault(
                openRouterIdentifier, predefinedResponses.get("default"));
        
        // Select a random response
        String fullResponse = possibleResponses.get(
                (int) (Math.random() * possibleResponses.size()));
        
        // Split into tokens (simple word splitting for the mock)
        String[] tokens = fullResponse.split("\\s+");
        
        // Schedule token emissions
        ScheduledFuture<?> future = scheduler.schedule(
                new TokenEmissionTask(tokens, 0, tokenConsumer, completionListener, requestId),
                getRandomInitialDelay(),
                TimeUnit.MILLISECONDS);
        
        activeGenerations.put(requestId, future);
        return requestId;
    }

    @Override
    public boolean cancelGeneration(UUID requestId) {
        ScheduledFuture<?> future = activeGenerations.remove(requestId);
        if (future != null && !future.isDone()) {
            log.debug("Cancelling mock text generation with ID {}", requestId);
            future.cancel(true);
            return true;
        }
        return false;
    }
    
    private long getRandomInitialDelay() {
        return INITIAL_DELAY_MS + (long) (Math.random() * VARIABLE_DELAY_MS);
    }
    
    private long getRandomDelay() {
        return DEFAULT_DELAY_MS + (long) (Math.random() * VARIABLE_DELAY_MS);
    }
    
    /**
     * Task for emitting tokens with delays
     */
    private class TokenEmissionTask implements Runnable {
        private final String[] tokens;
        private final int currentIndex;
        private final Consumer<String> tokenConsumer;
        private final Runnable completionListener;
        private final UUID requestId;
        
        TokenEmissionTask(
                String[] tokens,
                int currentIndex,
                Consumer<String> tokenConsumer,
                Runnable completionListener,
                UUID requestId) {
            this.tokens = tokens;
            this.currentIndex = currentIndex;
            this.tokenConsumer = tokenConsumer;
            this.completionListener = completionListener;
            this.requestId = requestId;
        }
        
        @Override
        public void run() {
            // Emit current token
            tokenConsumer.accept(tokens[currentIndex]);
            
            // Schedule next token or complete
            if (currentIndex < tokens.length - 1) {
                ScheduledFuture<?> future = scheduler.schedule(
                        new TokenEmissionTask(
                                tokens, currentIndex + 1, tokenConsumer, completionListener, requestId),
                        getRandomDelay(),
                        TimeUnit.MILLISECONDS);
                
                activeGenerations.put(requestId, future);
            } else {
                // All tokens emitted, call completion listener
                activeGenerations.remove(requestId);
                completionListener.run();
                log.debug("Completed mock text generation with ID {}", requestId);
            }
        }
    }
} 