package com.skybook.booking;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PnrGenerator {
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        char[] value = new char[8];
        for (int i = 0; i < value.length; i++) {
            value[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(value);
    }
}
