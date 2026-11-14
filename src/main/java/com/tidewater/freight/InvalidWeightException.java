package com.tidewater.freight;

// Signals that a shipment weight is not a positive finite number.
public class InvalidWeightException extends TidewaterException {

    public InvalidWeightException(double weight) {
        super("Weight must be a positive finite number but was: " + weight);
    }
}
