package com.v4r1n.uniantiseedcracker.config;

import java.util.Arrays;
import java.util.List;

/**
 * Common validators for config values
 */
public final class ConfigValidators {

    private ConfigValidators() {
        // Utility class
    }

    /**
     * Validates that value is a boolean
     */
    public static final ConfigValidator BOOLEAN = value -> {
        if (value instanceof Boolean) {
            return ValidationResult.success();
        }
        return ValidationResult.failure("Expected boolean, got " + (value == null ? "null" : value.getClass().getSimpleName()));
    };

    /**
     * Validates that value is a string
     */
    public static final ConfigValidator STRING = value -> {
        if (value instanceof String) {
            return ValidationResult.success();
        }
        return ValidationResult.failure("Expected string, got " + (value == null ? "null" : value.getClass().getSimpleName()));
    };

    /**
     * Validates that value is an integer
     */
    public static final ConfigValidator INTEGER = value -> {
        if (value instanceof Integer) {
            return ValidationResult.success();
        }
        return ValidationResult.failure("Expected integer, got " + (value == null ? "null" : value.getClass().getSimpleName()));
    };

    /**
     * Validates that value is a number (int, long, double, float)
     */
    public static final ConfigValidator NUMBER = value -> {
        if (value instanceof Number) {
            return ValidationResult.success();
        }
        return ValidationResult.failure("Expected number, got " + (value == null ? "null" : value.getClass().getSimpleName()));
    };

    /**
     * Validates that value is a list
     */
    public static final ConfigValidator LIST = value -> {
        if (value instanceof List) {
            return ValidationResult.success();
        }
        return ValidationResult.failure("Expected list, got " + (value == null ? "null" : value.getClass().getSimpleName()));
    };

    /**
     * Validates that value is a list of strings
     */
    public static final ConfigValidator STRING_LIST = value -> {
        if (!(value instanceof List)) {
            return ValidationResult.failure("Expected list, got " + (value == null ? "null" : value.getClass().getSimpleName()));
        }

        List<?> list = (List<?>) value;
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof String)) {
                return ValidationResult.failure("List item at index " + i + " is not a string");
            }
        }
        return ValidationResult.success();
    };

    /**
     * Validates that value is a non-empty string
     */
    public static final ConfigValidator NON_EMPTY_STRING = value -> {
        if (!(value instanceof String)) {
            return ValidationResult.failure("Expected string, got " + (value == null ? "null" : value.getClass().getSimpleName()));
        }
        if (((String) value).trim().isEmpty()) {
            return ValidationResult.failure("String must not be empty");
        }
        return ValidationResult.success();
    };

    /**
     * Validates that value is a string in the given list
     */
    public static ConfigValidator stringInList(String... validValues) {
        return value -> {
            if (!(value instanceof String)) {
                return ValidationResult.failure("Expected string, got " + (value == null ? "null" : value.getClass().getSimpleName()));
            }

            String strValue = (String) value;
            for (String valid : validValues) {
                if (valid.equalsIgnoreCase(strValue)) {
                    return ValidationResult.success();
                }
            }
            return ValidationResult.failure("Value must be one of: " + Arrays.toString(validValues));
        };
    }

    /**
     * Validates that integer is within range (inclusive)
     */
    public static ConfigValidator intInRange(int min, int max) {
        return value -> {
            if (!(value instanceof Integer)) {
                return ValidationResult.failure("Expected integer, got " + (value == null ? "null" : value.getClass().getSimpleName()));
            }

            int intValue = (Integer) value;
            if (intValue < min || intValue > max) {
                return ValidationResult.failure("Value must be between " + min + " and " + max);
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that number is positive
     */
    public static final ConfigValidator POSITIVE_NUMBER = value -> {
        if (!(value instanceof Number)) {
            return ValidationResult.failure("Expected number, got " + (value == null ? "null" : value.getClass().getSimpleName()));
        }

        double numValue = ((Number) value).doubleValue();
        if (numValue <= 0) {
            return ValidationResult.failure("Value must be positive");
        }
        return ValidationResult.success();
    };

    /**
     * Validates that number is non-negative
     */
    public static final ConfigValidator NON_NEGATIVE_NUMBER = value -> {
        if (!(value instanceof Number)) {
            return ValidationResult.failure("Expected number, got " + (value == null ? "null" : value.getClass().getSimpleName()));
        }

        double numValue = ((Number) value).doubleValue();
        if (numValue < 0) {
            return ValidationResult.failure("Value must be non-negative");
        }
        return ValidationResult.success();
    };

    /**
     * Combines multiple validators (all must pass)
     */
    public static ConfigValidator all(ConfigValidator... validators) {
        return value -> {
            for (ConfigValidator validator : validators) {
                ValidationResult result = validator.validate(value);
                if (!result.isValid()) {
                    return result;
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Combines multiple validators (at least one must pass)
     */
    public static ConfigValidator any(ConfigValidator... validators) {
        return value -> {
            StringBuilder errors = new StringBuilder();
            for (ConfigValidator validator : validators) {
                ValidationResult result = validator.validate(value);
                if (result.isValid()) {
                    return ValidationResult.success();
                }
                if (errors.length() > 0) {
                    errors.append(", ");
                }
                errors.append(result.getMessage());
            }
            return ValidationResult.failure("None of the validators passed: " + errors);
        };
    }
}
