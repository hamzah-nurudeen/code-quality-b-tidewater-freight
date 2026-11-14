package com.tidewater.freight.exception;

// Signals that a shipment weight is not a positive finite number.
public class InvalidWeightException extends TidewaterException {

    private static final long serialVersionUID = 1L;

    public InvalidWeightException(double weight) {
        super("Weight must be a positive finite number but was: " + weight);
    }
}
