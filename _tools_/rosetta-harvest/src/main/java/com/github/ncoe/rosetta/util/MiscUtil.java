package com.github.ncoe.rosetta.util;

/**
 * Random stuff requested.
 */
public final class MiscUtil {
    private MiscUtil() {
        throw new AssertionError("No instance for you!");
    }

    /**
     * @param predicate the predicate to use to select a value
     * @param a         the value to use when the predicate is true
     * @param b         the value to use when the predicate is false
     * @param <T>       the type of value that will be processed
     * @return the selected value
     */
    public static <T> T choice(boolean predicate, T a, T b) {
        if (predicate) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * @param condition the condition to check
     * @param message   the message to display
     */
    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}
