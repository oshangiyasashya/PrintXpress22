package com.printxpress.util;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Enterprise-grade Password Hashing Utility.
 * Uses PBKDF2WithHmacSHA256 with salt and constant-time equality checks.
 * Backward-compatible with legacy SHA-256 seed hashes.
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 12000;
    private static final int KEY_LENGTH = 256;
    private static final String PBKDF2_PREFIX = "PBKDF2$";

    private PasswordUtil() {}

    private static String encodeBase64(byte[] data) {
        try {
            return java.util.Base64.getEncoder().encodeToString(data);
        } catch (Throwable t) {
            return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
        }
    }

    private static byte[] decodeBase64(String str) {
        try {
            return java.util.Base64.getDecoder().decode(str);
        } catch (Throwable t) {
            return android.util.Base64.decode(str, android.util.Base64.NO_WRAP);
        }
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return encodeBase64(salt);
    }

    /**
     * Hashes password using PBKDF2WithHmacSHA256 with salt.
     */
    public static String hashPassword(String password, String salt) {
        try {
            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    decodeBase64(salt),
                    ITERATIONS,
                    KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return PBKDF2_PREFIX + encodeBase64(hash);
        } catch (Exception e) {
            // Fallback to SHA-256 if PBKDF2 algorithm is unavailable on legacy device
            return hashSha256(password, salt);
        }
    }

    /**
     * Legacy SHA-256 hashing for backward compatibility.
     */
    public static String hashSha256(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(decodeBase64(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return encodeBase64(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing", e);
        }
    }

    /**
     * Verifies password using constant-time comparison.
     * Supports both PBKDF2 and legacy SHA-256 hashes.
     */
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        if (password == null || salt == null || storedHash == null) {
            return false;
        }

        if (storedHash.startsWith(PBKDF2_PREFIX)) {
            String calculated = hashPassword(password, salt);
            return MessageDigest.isEqual(
                    calculated.getBytes(),
                    storedHash.getBytes()
            );
        } else {
            // Legacy SHA-256 check
            String legacyCalculated = hashSha256(password, salt);
            return MessageDigest.isEqual(
                    legacyCalculated.getBytes(),
                    storedHash.getBytes()
            );
        }
    }

    /**
     * Check if stored password needs to be upgraded from legacy SHA-256 to PBKDF2.
     */
    public static boolean isLegacyHash(String storedHash) {
        return storedHash != null && !storedHash.startsWith(PBKDF2_PREFIX);
    }
}
