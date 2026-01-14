package com.bankapp.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final Pattern STRONG_PASSWORD =
            Pattern.compile(
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
            );

    public static void validate(String password) {
        if (password == null || !STRONG_PASSWORD.matcher(password).matches()) {
            throw new RuntimeException(
                    "Password must be at least 8 characters long and contain " +
                            "uppercase, lowercase, number, and special character"
            );
        }
    }
}
