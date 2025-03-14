package com.service.core.services.exceptions;

public class SettingsNotFoundException extends RuntimeException {

    public SettingsNotFoundException() {
        super("Settings not found!");
    }
}
