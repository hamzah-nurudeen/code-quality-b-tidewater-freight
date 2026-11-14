package com.tidewater.freight.exception;

// Signals that the rate source failed for a reason other than an unknown zone.
public class RateUnavailableException extends TidewaterException {

    private static final long serialVersionUID = 1L;

    public RateUnavailableException(String message) {
        super(message);
    }

    public RateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
