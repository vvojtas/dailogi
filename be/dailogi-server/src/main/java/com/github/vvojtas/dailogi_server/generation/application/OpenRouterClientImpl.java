package com.github.vvojtas.dailogi_server.generation.application;

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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
                true  // Enable streaming
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
                .filter(event -> event.startsWith("data:"))
                .map(event -> event.substring(5).trim())  // Remove "data: " prefix
                .filter(json -> !json.equals("[DONE]"))
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
            // Using regex for simple parsing without full deserialization
            Pattern pattern = Pattern.compile("\"content\":\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(json);
            
            if (matcher.find()) {
                String content = matcher.group(1);
                // Unescape JSON string content
                content = content.replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n")
                        .replace("\\t", "\t")
                        .replace("\\r", "\r");
                return Mono.just(content);
            }
            
            return Mono.empty();
        } catch (Exception e) {
            log.error("Failed to parse streaming response: {}", json, e);
            return Mono.empty();
        }
    }
} 