package com.service.core.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.security.SecureRandom;

public class CustomIdGenerator {
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SIZE = 6;

    public static String generate() {
        return NanoIdUtils.randomNanoId(RANDOM, ALPHABET, SIZE);
    }
}
