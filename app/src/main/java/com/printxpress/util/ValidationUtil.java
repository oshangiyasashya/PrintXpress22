package com.printxpress.util;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Enterprise Validation & Sanitization Utility.
 * Zero external libraries, 100% Java-based.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    );

    private static final Pattern SL_PHONE_PATTERN = Pattern.compile(
            "^(?:0|\\+94)?7[0-9]{8}$"
    );

    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile(
            "^[0-9]{5}$"
    );

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String clean = phone.trim().replaceAll("[\\s-]", "");
        return SL_PHONE_PATTERN.matcher(clean).matches();
    }

    public static boolean isValidPostalCode(String postalCode) {
        return postalCode != null && POSTAL_CODE_PATTERN.matcher(postalCode.trim()).matches();
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) return false;
        boolean hasDigit = false;
        boolean hasLetter = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
        }
        return hasDigit && hasLetter;
    }

    /**
     * Computes a strength score from 0 to 100.
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        int length = password.length();

        if (length >= 6) score += 20;
        if (length >= 8) score += 20;
        if (length >= 12) score += 15;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        if (hasUpper && hasLower) score += 15;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 15;

        return Math.min(100, score);
    }

    /**
     * Sanitizes user input to prevent HTML/script injection.
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Luhn Algorithm check for payment card numbers.
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String clean = cardNumber.replaceAll("[\\s-]", "");
        if (clean.length() < 13 || clean.length() > 19) return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = clean.length() - 1; i >= 0; i--) {
            char c = clean.charAt(i);
            if (!Character.isDigit(c)) return false;
            int n = c - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * Validates card expiry format (MM/YY) and ensures date is not in the past.
     */
    public static boolean isValidExpiryDate(String expiry) {
        if (expiry == null || !expiry.matches("^(0[1-9]|1[0-2])\\/([0-9]{2})$")) {
            return false;
        }
        try {
            String[] parts = expiry.split("/");
            int expMonth = Integer.parseInt(parts[0]);
            int expYear = 2000 + Integer.parseInt(parts[1]);

            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH) + 1; // 1-based

            if (expYear < currentYear) return false;
            if (expYear == currentYear && expMonth < currentMonth) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("^[0-9]{3,4}$");
    }
}
