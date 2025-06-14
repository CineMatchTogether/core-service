package com.service.core.services.exceptions;

public class SessionNotFoundException extends Exception{

    public SessionNotFoundException(String id) {
        super("Not found session with id: " + id);
    }
}
