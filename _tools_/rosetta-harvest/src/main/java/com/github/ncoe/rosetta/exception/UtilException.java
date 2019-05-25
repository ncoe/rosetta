package com.github.ncoe.rosetta.exception;

/**
 * For avoiding having to annotate every method with the exceptions it throws
 */
public class UtilException extends RuntimeException {
    public UtilException(Throwable cause) {
        super(cause);
    }
}
