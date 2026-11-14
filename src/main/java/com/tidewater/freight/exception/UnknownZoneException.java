package com.tidewater.freight.exception;

// Signals that a shipment was requested for a zone that has no rate.
public class UnknownZoneException extends TidewaterException {

    private static final long serialVersionUID = 1L;

    private final String zone;

    public UnknownZoneException(String zone) {
        super("Unknown zone: " + zone);
        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }
}
