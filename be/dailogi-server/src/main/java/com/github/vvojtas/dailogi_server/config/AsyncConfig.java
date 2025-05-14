package com.github.vvojtas.dailogi_server.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for asynchronous task execution.
 * This improves the handling of SSE streaming by providing a dedicated thread pool.
 */
@EnableAsync
@Configuration
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Creating async task executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Set the core pool size to 5 (for basic tasks)
        executor.setCorePoolSize(5);
        // Maximum of 10 threads for handling multiple concurrent dialogues
        executor.setMaxPoolSize(10);
        // Queue maximum of 25 pending tasks
        executor.setQueueCapacity(25);
        // Thread name prefix - helps with identification in logs/diagnostic tools
        executor.setThreadNamePrefix("DialogExecutor-");
        // Explicitly call initialize() method to avoid the deprecation warning
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
} 