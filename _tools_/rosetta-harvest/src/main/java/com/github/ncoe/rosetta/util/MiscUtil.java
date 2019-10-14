package com.github.ncoe.rosetta.util;

public final class MiscUtil {
    private MiscUtil() {
        throw new AssertionError("No instance for you!");
    }

    public static <T> T choice(boolean predicate, T a, T b) {
        if (predicate) {
            return a;
        } else {
            return b;
        }
    }
}
