package com.service.core.security.services.exception;

public class UserNotFoundException extends Exception{

    public UserNotFoundException(String login, String email) {
        super(String.format("User with login %s or email %s not found", login, email));
    }
}
