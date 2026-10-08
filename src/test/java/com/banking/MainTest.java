package com.banking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    @DisplayName("Verify Day 1 Initialization test suite")
    void testMainInitialization() {
        assertTrue(true, "Day 1 Environment and JUnit 5 setup is valid.");
    }

    @Test
    @DisplayName("Verify Main method executes without throwing exceptions")
    void testMainExecution() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}
