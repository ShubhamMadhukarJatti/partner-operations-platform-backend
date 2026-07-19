package com.sharkdom.util;

import java.security.SecureRandom;

public class RandomGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate9DigitAlphaNumeric() {
        StringBuilder builder = new StringBuilder(9);

        for (int i = 0; i < 9; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }

        return builder.toString();
    }

}