package com.goldtrader.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {
        // Utility class
    }

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean verifyPassword(String password, String hash) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        return result.verified;
    }
}
