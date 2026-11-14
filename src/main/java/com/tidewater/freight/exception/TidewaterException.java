package com.tidewater.freight.exception;

// Base type for every failure the shipping service reports to its callers.
public class TidewaterException extends RuntimeException {

    public TidewaterException(String message) {
        super(message);
    }

    public TidewaterException(String message, Throwable cause) {
        super(message, cause);
    }
}
