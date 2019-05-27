package com.github.ncoe.rosetta.exception;

/**
 * For avoiding having to annotate every method with the exceptions it throws.
 */
public class UtilException extends RuntimeException {
    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of
     * {@code cause}).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public UtilException(Throwable cause) {
        super(cause);
    }
}
