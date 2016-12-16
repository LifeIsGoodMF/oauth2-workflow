package com.test.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SecureIDGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecureIDGenerator() {}

    public static String generateId() {
        return generateId(100);
    }
    public static String generateId(int bits) {
        return new BigInteger(bits, RANDOM).toString(32);
    }
}
