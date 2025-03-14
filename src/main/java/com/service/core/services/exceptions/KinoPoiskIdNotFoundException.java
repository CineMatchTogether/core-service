package com.service.core.services.exceptions;

public class KinoPoiskIdNotFoundException extends Exception {

    public KinoPoiskIdNotFoundException(String username) {
        super("KinoPoisk id's " + username + " not found!");
    }

    public KinoPoiskIdNotFoundException() {
    }
}
