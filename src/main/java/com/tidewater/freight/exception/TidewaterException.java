package com.tidewater.freight.exception;

// Base type for every failure the shipping service reports to its callers.
public class TidewaterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TidewaterException(String message) {
        super(message);
    }

    public TidewaterException(String message, Throwable cause) {
        super(message, cause);
    }
}
