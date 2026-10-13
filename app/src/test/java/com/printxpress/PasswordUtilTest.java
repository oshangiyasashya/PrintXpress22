package com.printxpress;

import com.printxpress.util.PasswordUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordUtilTest {

    @Test
    public void testHashAndVerifyPasswordPBKDF2() {
        String password = "SecureAdminPassword!2026";
        String salt = PasswordUtil.generateSalt();

        assertNotNull(salt);
        assertFalse(salt.isEmpty());

        String hash = PasswordUtil.hashPassword(password, salt);
        assertNotNull(hash);
        assertFalse(hash.isEmpty());

        // Verify correct password
        boolean verified = PasswordUtil.verifyPassword(password, salt, hash);
        assertTrue("Password should verify correctly with PBKDF2", verified);

        // Verify incorrect password
        boolean wrongPasswordVerified = PasswordUtil.verifyPassword("WrongPassword123", salt, hash);
        assertFalse("Wrong password must fail verification", wrongPasswordVerified);
    }

    @Test
    public void testDifferentSaltsProduceDifferentHashes() {
        String password = "SamePassword123";
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        assertNotEquals("Generated salts must be distinct", salt1, salt2);

        String hash1 = PasswordUtil.hashPassword(password, salt1);
        String hash2 = PasswordUtil.hashPassword(password, salt2);

        assertNotEquals("Different salts must produce different hashes", hash1, hash2);
    }

    @Test
    public void testLegacySha256VerificationFallback() {
        // Precomputed legacy SHA-256 hash of "admin123" + "adminsalt123"
        String password = "admin123";
        String salt = "adminsalt123";
        String legacyHash = PasswordUtil.hashSha256(password, salt);

        boolean verified = PasswordUtil.verifyPassword(password, salt, legacyHash);
        assertTrue("Legacy SHA-256 hash should verify cleanly via fallback", verified);

        boolean wrongVerified = PasswordUtil.verifyPassword("wrongpass", salt, legacyHash);
        assertFalse("Incorrect password on legacy hash must fail", wrongVerified);
    }

    @Test
    public void testEmptyOrNullPasswordHandling() {
        assertFalse(PasswordUtil.verifyPassword(null, "salt", "hash"));
        assertFalse(PasswordUtil.verifyPassword("pass", null, "hash"));
        assertFalse(PasswordUtil.verifyPassword("pass", "salt", null));
    }
}
