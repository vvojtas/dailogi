package com.github.vvojtas.dailogi_server.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationErrorException extends AuthenticationException {
    public AuthenticationErrorException(String msg) {
        super(msg);
    }
    
    public AuthenticationErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }
} 