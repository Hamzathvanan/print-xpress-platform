package com.printxpress.app.util;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_REGEX =
            Pattern.compile("^(?:\\+94|0)?7\\d{8}$");

    private Validation() {
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL_REGEX.matcher(value.trim()).matches();
    }

    public static boolean isValidPhone(String value) {
        if (value == null) {
            return false;
        }
        String normalized = normalizePhone(value);
        return PHONE_REGEX.matcher(normalized).matches();
    }

    public static boolean isValidPassword(String value) {
        return value != null && value.length() >= 6;
    }

    public static boolean isValidName(String value) {
        return value != null && value.trim().length() >= 2;
    }

    public static boolean isValidQuantity(String value) {
        try {
            int qty = Integer.parseInt(value);
            return qty >= 1 && qty <= 10000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String normalizePhone(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(" ", "").replace("-", "");
    }

    public static String passwordHash(String password) {
        // Coursework-local store — not a production crypto scheme.
        byte[] bytes = password.trim().getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
