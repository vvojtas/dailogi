package com.github.vvojtas.dailogi_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Web MVC configuration focused on improving async SSE streaming performance
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Configure async request processing specifically for SSE streams
     */
    @Override
    public void configureAsyncSupport(@NonNull AsyncSupportConfigurer configurer) {
        log.info("Configuring async support for SSE streaming");
        // Set timeout to 5 minutes (300000ms) for asynchronous requests
        // Increase this value if dialogues take longer
        configurer.setDefaultTimeout(1800000);
        
        // Use the same executor that we configured in AsyncConfig
        // configurer.setTaskExecutor(...); - Optional, Spring will use the taskExecutor bean by default
    }
    
    /**
     * Ensure proper cache control for SSE streams
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/dialogues/stream/**")
            .setCacheControl(CacheControl.noCache());
    }
} 