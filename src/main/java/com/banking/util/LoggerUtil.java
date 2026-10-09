package com.banking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Utility class for formatted logging across the application.
 * Supports log subscribers for UI activity logging.
 */
public class LoggerUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CopyOnWriteArrayList<Consumer<String>> LISTENERS = new CopyOnWriteArrayList<>();

    public static void addListener(Consumer<String> listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(Consumer<String> listener) {
        LISTENERS.remove(listener);
    }

    private static void notifyListeners(String logMsg) {
        for (Consumer<String> listener : LISTENERS) {
            try {
                listener.accept(logMsg);
            } catch (Exception ignored) {
                // Ignore listener exceptions
            }
        }
    }

    public static void info(String message) {
        String formatted = "[" + LocalDateTime.now().format(FORMATTER) + "] [INFO] " + message;
        System.out.println(formatted);
        notifyListeners(formatted);
    }

    public static void warn(String message) {
        String formatted = "[" + LocalDateTime.now().format(FORMATTER) + "] [WARN] " + message;
        System.out.println(formatted);
        notifyListeners(formatted);
    }

    public static void error(String message) {
        String formatted = "[" + LocalDateTime.now().format(FORMATTER) + "] [ERROR] " + message;
        System.err.println(formatted);
        notifyListeners(formatted);
    }
}
