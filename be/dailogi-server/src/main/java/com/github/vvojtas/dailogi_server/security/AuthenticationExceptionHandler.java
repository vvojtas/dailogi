package com.github.vvojtas.dailogi_server.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * This class bridges filter chain exceptions with the Spring MVC exception handling mechanism.
 * It delegates authentication exceptions from filters to the HandlerExceptionResolver,
 * which will then use our GlobalExceptionHandler.
 */
@Component
public class AuthenticationExceptionHandler implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public AuthenticationExceptionHandler(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Delegate to the HandlerExceptionResolver
        resolver.resolveException(request, response, null, authException);
    }
} 