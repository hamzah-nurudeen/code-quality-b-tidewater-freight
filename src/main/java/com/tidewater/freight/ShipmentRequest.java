package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.InvalidWeightException;

// Carries one validated shipment to be priced; invalid input cannot be constructed.
public class ShipmentRequest {

    private final String zone;
    private final double weightKg;
    private final ServiceLevel serviceLevel;

    public ShipmentRequest(String zone, double weightKg, ServiceLevel serviceLevel) {
        validateZone(zone);
        validateServiceLevel(serviceLevel);
        validateWeight(weightKg);
        this.zone = zone;
        this.weightKg = weightKg;
        this.serviceLevel = serviceLevel;
    }

    public String getZone() {
        return zone;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public ServiceLevel getServiceLevel() {
        return serviceLevel;
    }

    private static void validateZone(String zone) {
        if (zone == null || zone.trim().isEmpty()) {
            throw new InvalidShipmentException("Zone is required");
        }
    }

    private static void validateServiceLevel(ServiceLevel serviceLevel) {
        if (serviceLevel == null) {
            throw new InvalidShipmentException("Service level is required");
        }
    }

    private static void validateWeight(double weightKg) {
        if (Double.isNaN(weightKg) || Double.isInfinite(weightKg) || weightKg <= 0) {
            throw new InvalidWeightException(weightKg);
        }
    }
}
