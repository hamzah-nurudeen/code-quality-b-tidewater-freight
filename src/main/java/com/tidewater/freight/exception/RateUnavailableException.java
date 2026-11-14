package com.tidewater.freight.exception;

// Signals that the rate source failed for a reason other than an unknown zone.
public class RateUnavailableException extends TidewaterException {

    public RateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
