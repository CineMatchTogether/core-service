package com.service.core.security.services.exception;

public class EmptyTokenRefreshException extends Exception {
    public EmptyTokenRefreshException(String message) {
        super(message);
    }
}
