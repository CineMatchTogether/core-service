package com.service.core.security.services.exception;

import java.util.UUID;

public class UserNotFoundException extends Exception{

    public UserNotFoundException(String login, String email) {
        super(String.format("User with login %s or email %s not found", login, email));
    }

    public UserNotFoundException(UUID userId) {
        super(String.format("User with id %s not found", userId));
    }
}
