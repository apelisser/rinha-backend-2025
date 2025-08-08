package com.apelisser.rinha2025.core.util;

public final class ThreadUtil {

    private ThreadUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void sleep(int millis) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
