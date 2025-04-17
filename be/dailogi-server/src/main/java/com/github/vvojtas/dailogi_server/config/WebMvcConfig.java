package com.github.vvojtas.dailogi_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.io.Resource;
import java.io.IOException;
import org.springframework.web.servlet.resource.PathResourceResolver;
import com.github.vvojtas.dailogi_server.properties.ImgPathProperties;
import org.springframework.lang.NonNull;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ImgPathProperties imgPathProperties;

    public WebMvcConfig(ImgPathProperties imgPathProperties) {
        this.imgPathProperties = imgPathProperties;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations(imgPathProperties.getPath())
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        Resource requestedResource = location.createRelative("favico.ico");
                        return (requestedResource.exists() && requestedResource.isReadable()) ? requestedResource : null;
                    }
                });
    }
} 