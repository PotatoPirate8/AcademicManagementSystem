package com.academic.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil hashing and verification.
 */
class PasswordUtilTest {

    @Test
    void testHashPasswordConsistency() {
        String hash1 = PasswordUtil.hashPassword("password123");
        String hash2 = PasswordUtil.hashPassword("password123");
        assertEquals(hash1, hash2, "Same password should always produce the same hash");
    }

    @Test
    void testHashPasswordDifferentInputs() {
        String hash1 = PasswordUtil.hashPassword("password1");
        String hash2 = PasswordUtil.hashPassword("password2");
        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    void testHashPasswordLength() {
        String hash = PasswordUtil.hashPassword("test");
        assertEquals(64, hash.length(), "SHA-256 should produce a 64-character hex string");
    }

    @Test
    void testHashPasswordNotPlaintext() {
        String password = "mysecretpassword";
        String hash = PasswordUtil.hashPassword(password);
        assertNotEquals(password, hash, "Hash should not equal the original password");
    }

    @Test
    void testVerifyPasswordCorrect() {
        String hash = PasswordUtil.hashPassword("myPassword");
        assertTrue(PasswordUtil.verifyPassword("myPassword", hash));
    }

    @Test
    void testVerifyPasswordIncorrect() {
        String hash = PasswordUtil.hashPassword("myPassword");
        assertFalse(PasswordUtil.verifyPassword("wrongPassword", hash));
    }

    @Test
    void testKnownHashValue() {
        // Verify the admin default password hash is correct
        String hash = PasswordUtil.hashPassword("admin123");
        assertEquals("240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9", hash);
    }

    @Test
    void testEmptyStringHash() {
        String hash = PasswordUtil.hashPassword("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}
