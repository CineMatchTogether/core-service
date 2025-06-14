package com.service.core.services.exceptions;

public class CannotConnectToSessionException extends Exception {

    public CannotConnectToSessionException(String id) {
        super("You cant connect to runnable or archive session: " + id);
    }
}
