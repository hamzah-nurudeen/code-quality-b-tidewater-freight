package com.tidewater.freight;

// Signals that a shipment request is missing or has an invalid required field.
public class InvalidShipmentException extends TidewaterException {

    public InvalidShipmentException(String message) {
        super(message);
    }
}
