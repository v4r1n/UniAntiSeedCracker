package com.v4r1n.uniantiseedcracker.config;

/**
 * Functional interface for config value validation
 */
@FunctionalInterface
public interface ConfigValidator {
    ValidationResult validate(Object value);
}
