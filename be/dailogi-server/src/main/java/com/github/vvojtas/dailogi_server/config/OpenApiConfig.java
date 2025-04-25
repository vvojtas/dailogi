package com.github.vvojtas.dailogi_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dailogiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("d-AI-logi API")
                        .description("API for d-AI-logi - an interactive web application for creating AI dialogues")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("d-AI-logi repo")
                                .url("https://github.com/vvojtas/dailogi")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication")));
    }
    
    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("api")
                .packagesToScan("com.github.vvojtas.dailogi_server.controller")
                .pathsToMatch("/api/**")
                .build();
    }
} 