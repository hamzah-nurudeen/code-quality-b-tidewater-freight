package com.tidewater.freight.exception;

// Signals that a shipment request is missing or has an invalid required field.
public class InvalidShipmentException extends TidewaterException {

    private static final long serialVersionUID = 1L;

    public InvalidShipmentException(String message) {
        super(message);
    }
}
