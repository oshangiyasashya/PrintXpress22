package com.printxpress;

import com.printxpress.util.ValidationUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUtilTest {

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("customer@printxpress.lk"));
        assertTrue(ValidationUtil.isValidEmail("john.doe@sub.example.com"));
        assertTrue(ValidationUtil.isValidEmail("admin123@gmail.com"));

        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("notanemail"));
        assertFalse(ValidationUtil.isValidEmail("missing@domain"));
        assertFalse(ValidationUtil.isValidEmail("@missingusername.com"));
        assertFalse(ValidationUtil.isValidEmail("spaces in@email.com"));
    }

    @Test
    public void testSriLankaPhoneValidation() {
        assertTrue(ValidationUtil.isValidPhone("0712345678"));
        assertTrue(ValidationUtil.isValidPhone("0779876543"));
        assertTrue(ValidationUtil.isValidPhone("+94771234567"));
        assertTrue(ValidationUtil.isValidPhone("0701122334"));

        assertFalse(ValidationUtil.isValidPhone(null));
        assertFalse(ValidationUtil.isValidPhone(""));
        assertFalse(ValidationUtil.isValidPhone("0112345678")); // Landline
        assertFalse(ValidationUtil.isValidPhone("071234567")); // Too short (9 digits)
        assertFalse(ValidationUtil.isValidPhone("07123456789")); // Too long (11 digits)
        assertFalse(ValidationUtil.isValidPhone("07abcdefgh")); // Letters
    }

    @Test
    public void testPostalCodeValidation() {
        assertTrue(ValidationUtil.isValidPostalCode("00300"));
        assertTrue(ValidationUtil.isValidPostalCode("10100"));

        assertFalse(ValidationUtil.isValidPostalCode(null));
        assertFalse(ValidationUtil.isValidPostalCode("300"));
        assertFalse(ValidationUtil.isValidPostalCode("003001"));
        assertFalse(ValidationUtil.isValidPostalCode("ABCDE"));
    }

    @Test
    public void testPasswordStrength() {
        // Strong password
        assertTrue(ValidationUtil.isStrongPassword("P@ssw0rd2026"));
        assertTrue(ValidationUtil.isStrongPassword("admin123"));

        // Weak / short password
        assertFalse(ValidationUtil.isStrongPassword("12345"));
        assertFalse(ValidationUtil.isStrongPassword("abcde"));
        assertFalse(ValidationUtil.isStrongPassword("123456")); // No letter
        assertFalse(ValidationUtil.isStrongPassword("abcdef")); // No digit
        assertFalse(ValidationUtil.isStrongPassword(null));

        int weakScore = ValidationUtil.calculatePasswordStrength("123");
        int strongScore = ValidationUtil.calculatePasswordStrength("PxStudio@Colombo2026!");
        assertTrue("Strong score must exceed weak score", strongScore > weakScore);
        assertTrue("Strong score should be at least 70", strongScore >= 70);
    }

    @Test
    public void testSanitizeInput() {
        String unsafe = "<script>alert('xss');</script>";
        String safe = ValidationUtil.sanitize(unsafe);

        assertFalse("Sanitized string must not contain raw script tags", safe.contains("<"));
        assertFalse("Sanitized string must not contain raw script tags", safe.contains(">"));
        assertTrue(safe.contains("&lt;script&gt;"));

        assertEquals("", ValidationUtil.sanitize(null));
    }

    @Test
    public void testLuhnCardValidation() {
        // Known valid test card numbers (Luhn compliant, 16-digit Visa)
        assertTrue(ValidationUtil.isValidCardNumber("4111111111111111"));
        assertTrue(ValidationUtil.isValidCardNumber("4111 1111 1111 1111")); // Spaced

        // Invalid cards
        assertFalse(ValidationUtil.isValidCardNumber("4111111111111112")); // Invalid checksum
        assertFalse(ValidationUtil.isValidCardNumber(null));
        assertFalse(ValidationUtil.isValidCardNumber("123")); // Too short
        assertFalse(ValidationUtil.isValidCardNumber("lettersnotnumbers"));
    }

    @Test
    public void testExpiryDateValidation() {
        // Expiry format MM/YY
        assertTrue(ValidationUtil.isValidExpiryDate("12/35")); // Future year 2035
        assertTrue(ValidationUtil.isValidExpiryDate("01/40")); // Future year 2040

        // Past dates
        assertFalse(ValidationUtil.isValidExpiryDate("01/20")); // Expired 2020
        assertFalse(ValidationUtil.isValidExpiryDate("05/18")); // Expired 2018

        // Invalid formats
        assertFalse(ValidationUtil.isValidExpiryDate("13/28")); // Invalid month 13
        assertFalse(ValidationUtil.isValidExpiryDate("00/28")); // Invalid month 00
        assertFalse(ValidationUtil.isValidExpiryDate("2026/12")); // Wrong format
        assertFalse(ValidationUtil.isValidExpiryDate(null));
    }

    @Test
    public void testCvvValidation() {
        assertTrue(ValidationUtil.isValidCvv("123"));
        assertTrue(ValidationUtil.isValidCvv("1234"));

        assertFalse(ValidationUtil.isValidCvv(null));
        assertFalse(ValidationUtil.isValidCvv("12"));
        assertFalse(ValidationUtil.isValidCvv("12345"));
        assertFalse(ValidationUtil.isValidCvv("12a"));
    }
}
