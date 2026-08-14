package com.cheng.game.app.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionSecretValidatorTest {

    @Test
    void rejectsWeakDefaults() {
        assertThrows(IllegalStateException.class,
                () -> ProductionSecretValidator.validate("change-me-to-a-very-long-secret-key-32bytes!", "ops"));
        assertThrows(IllegalStateException.class,
                () -> ProductionSecretValidator.validate("a-strong-production-secret-key-32b", "dev-ops-token"));
        assertThrows(IllegalStateException.class,
                () -> ProductionSecretValidator.validate("short", "unique-ops"));
    }

    @Test
    void acceptsStrongSecrets() {
        assertDoesNotThrow(() -> ProductionSecretValidator.validate(
                "a-strong-production-secret-key-32b",
                "unique-ops-token"));
    }
}
