package com.test.util;

import java.util.concurrent.ThreadLocalRandom;

public class PasswordGenerator {

    private static final char[] symbols = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final static ThreadLocalRandom random = ThreadLocalRandom.current();

    public static String generatePassword(int length) {
        final char[] buf = new char[length];
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }
}
