package com.banking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for formatted logging across the application.
 */
public class LoggerUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [INFO] " + message);
    }

    public static void warn(String message) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [WARN] " + message);
    }

    public static void error(String message) {
        System.err.println("[" + LocalDateTime.now().format(FORMATTER) + "] [ERROR] " + message);
    }
}
