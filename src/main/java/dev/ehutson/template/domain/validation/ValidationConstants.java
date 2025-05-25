package dev.ehutson.template.domain.validation;

/**
 * Centralized validation constants and patterns for domain models.
 * This approach keeps all validation rules in one place for easy maintenance.
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Utility class
    }

    // Username validation
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]+$";
    public static final String USERNAME_MESSAGE = "Username can only contain letters, numbers, dots, dashes, and underscores";
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;

    // Password validation (for raw passwords, not hashed)
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$";
    public static final String PASSWORD_MESSAGE = "Password must be at least 8 characters with uppercase, lowercase, and number";
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;

    // Email validation
    public static final int EMAIL_MIN_LENGTH = 5;
    public static final int EMAIL_MAX_LENGTH = 254; // RFC 5321 standard
    public static final String EMAIL_MESSAGE = "Please provide a valid email address";

    // Name validation
    public static final int NAME_MAX_LENGTH = 50;
    public static final String NAME_MESSAGE = "Name cannot exceed 50 characters";

    // Language and timezone
    public static final int LANG_KEY_MIN_LENGTH = 2;
    public static final int LANG_KEY_MAX_LENGTH = 10;
    public static final int TIMEZONE_MAX_LENGTH = 50;

    // Token validation (activation, reset keys)
    public static final int TOKEN_MAX_LENGTH = 20;

    // Common validation groups (for conditional validation)
    public interface Create {
    }

    public interface Update {
    }

    public interface PasswordReset {
    }

    /**
     * Validation utility methods for testing and business logic.
     */
    public static class Utils {

        private Utils() {
            // Utility class
        }

        public static boolean isValidUsername(String username) {
            return username != null &&
                    username.length() >= USERNAME_MIN_LENGTH &&
                    username.length() <= USERNAME_MAX_LENGTH &&
                    username.matches(USERNAME_PATTERN);
        }

        public static boolean isValidEmail(String email) {
            return email != null &&
                    email.length() >= EMAIL_MIN_LENGTH &&
                    email.length() <= EMAIL_MAX_LENGTH &&
                    email.contains("@") && email.contains(".");
        }

        public static boolean isValidPassword(String password) {
            return password != null &&
                    password.length() >= PASSWORD_MIN_LENGTH &&
                    password.length() <= PASSWORD_MAX_LENGTH &&
                    password.matches(PASSWORD_PATTERN);
        }
    }
}