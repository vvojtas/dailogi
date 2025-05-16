package com.github.vvojtas.dailogi_server.config;

import com.github.vvojtas.dailogi_server.properties.OpenRouterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration for OpenRouter API client
 */
@Configuration
@RequiredArgsConstructor
public class OpenRouterConfig {
    
    private final OpenRouterProperties properties;
    
    /**
     * Creates a WebClient bean configured for OpenRouter API
     * @param builder WebClient.Builder to use
     * @return Configured WebClient
     */
    @Bean
    public WebClient openRouterWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, "text/event-stream")
                .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeout().toMillis())
                        .responseTimeout(properties.getReadTimeout())
                ))
                .build();
    }
} 