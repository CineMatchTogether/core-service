package com.service.core.security.services.exception;

public class TokenRefreshException extends Exception {

    public TokenRefreshException(String message) {
        super(String.format(message));
    }
}
