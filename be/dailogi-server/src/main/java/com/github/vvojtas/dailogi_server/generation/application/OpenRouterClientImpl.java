package com.github.vvojtas.dailogi_server.generation.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vvojtas.dailogi_server.generation.api.ChatMessage;
import com.github.vvojtas.dailogi_server.generation.api.OpenRouterInterface;
import com.github.vvojtas.dailogi_server.generation.application.model.ChatCompletionRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Real implementation of OpenRouterInterface for production use.
 * Makes actual API requests to the OpenRouter API.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "openrouter.api", name = "mock-enabled", havingValue = "false", matchIfMissing = true)
public class OpenRouterClientImpl implements OpenRouterInterface {

    private final WebClient openRouterWebClient;
    private final Map<UUID, Boolean> activeCalls = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public UUID streamChat(
            String openRouterIdentifier,
            List<ChatMessage> messages,
            String apiKey,
            Consumer<String> tokenConsumer,
            Runnable completionListener) {
        
        UUID requestId = UUID.randomUUID();
        log.debug("Starting OpenRouter API call with ID {}", requestId);
        
        // Mark this request as active
        activeCalls.put(requestId, true);
        
        // Validate inputs
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Cannot send request: API key not provided");
            completionListener.run();
            return requestId;
        }
        
        if (messages == null || messages.isEmpty()) {
            log.error("Cannot send request: No messages provided");
            completionListener.run();
            return requestId;
        }
        
        // Create request body
        ChatCompletionRequest requestBody = new ChatCompletionRequest(
                openRouterIdentifier,
                messages,
                true,
                1000,
                new ChatCompletionRequest.ReasoningConfig(true)
        );
        
        // Make API request
        openRouterWebClient
                .post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, "text/event-stream")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.error(new RuntimeException("Invalid API key"));
                    } else if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        return Mono.error(new RuntimeException("Rate limit exceeded"));
                    }
                    return response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Client error: " + response.statusCode() + " - " + body)));
                })
                .onStatus(status -> status.is5xxServerError(), response -> 
                    response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Server error: " + response.statusCode() + " - " + body)))
                )
                .bodyToFlux(String.class)
                .doOnEach(signal -> {
                    if (signal.isOnNext()) {
                        log.info("ROUTER event: {}", signal.get());
                    }
                })
                .map(event -> {
                    // Handle both SSE format (data: prefix) and raw JSON
                    if (event.startsWith("data:")) {
                        return event.substring(5).trim(); // Remove "data: " prefix
                    }
                    return event.trim(); // Keep as is if no prefix
                })
                .filter(json -> !json.isEmpty() && !json.equals("[DONE]"))
                .flatMap(this::parseStreamingResponse)
                .takeWhile(content -> isRequestActive(requestId))
                .doOnNext(tokenConsumer)
                .doOnComplete(() -> {
                    if (isRequestActive(requestId)) {
                        activeCalls.remove(requestId);
                        completionListener.run();
                        log.debug("Completed OpenRouter API call with ID {}", requestId);
                    }
                })
                .doOnError(error -> {
                    log.error("Error in OpenRouter API call with ID {}: {}", requestId, error.getMessage());
                    activeCalls.remove(requestId);
                    completionListener.run();
                })
                .subscribe();
        
        return requestId;
    }

    @Override
    public boolean cancelGeneration(UUID requestId) {
        boolean wasActive = activeCalls.remove(requestId) != null;
        if (wasActive) {
            log.debug("Cancelled OpenRouter API call with ID {}", requestId);
        }
        return wasActive;
    }
    
    private boolean isRequestActive(UUID requestId) {
        return activeCalls.getOrDefault(requestId, false);
    }
    
    private Mono<String> parseStreamingResponse(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Mono.empty();
            }
            
            log.trace("Parsing streaming response: {}", json);
            JsonNode rootNode = objectMapper.readTree(json);
            
            // Get choices array
            JsonNode choices = rootNode.path("choices");
            if (choices.isEmpty() || !choices.isArray() || choices.size() == 0) {
                log.debug("No choices in response: {}", json);
                return Mono.empty();
            }
            
            // Get first choice
            JsonNode choice = choices.get(0);
            
            // Check for delta structure (streaming format)
            JsonNode delta = choice.path("delta");
            if (!delta.isMissingNode()) {
                JsonNode content = delta.path("content");
                if (!content.isMissingNode() && content.isTextual()) {
                    String token = content.asText();
                    log.trace("Extracted token: {}", token);
                    return Mono.just(token);
                }
            }
            
            // Check for standard message format as fallback
            JsonNode message = choice.path("message");
            if (!message.isMissingNode()) {
                JsonNode content = message.path("content");
                if (!content.isMissingNode() && content.isTextual()) {
                    String token = content.asText();
                    log.trace("Extracted content from message: {}", token);
                    return Mono.just(token);
                }
            }
            
            log.debug("No content found in response: {}", json);
            return Mono.empty();
        } catch (Exception e) {
            log.error("Failed to parse streaming response: {}", json, e);
            return Mono.empty();
        }
    }
} 
