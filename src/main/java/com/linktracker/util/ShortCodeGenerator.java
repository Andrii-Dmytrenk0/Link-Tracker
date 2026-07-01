package com.linktracker.util;

import java.security.SecureRandom;

/**
 * Generates random, URL-safe short codes used as influencer identifiers
 * in tracking links (e.g. {@code /i/a8K2xP}).
 */
public final class ShortCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 7;

    private ShortCodeGenerator() {
    }

    /**
     * Generates a random code of the default length (7 characters, ~6-8 range).
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generates a random alphanumeric code of the requested length.
     *
     * @param length number of characters, expected to be between 6 and 8
     */
    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
