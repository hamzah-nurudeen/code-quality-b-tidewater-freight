package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.InvalidWeightException;

import java.math.BigDecimal;

// Carries one validated shipment to be priced; invalid input cannot be constructed.
public final class ShipmentRequest {

    private final String zone;
    private final BigDecimal weightKg;
    private final ServiceLevel serviceLevel;

    public ShipmentRequest(String zone, BigDecimal weightKg, ServiceLevel serviceLevel) {
        validateZone(zone);
        validateServiceLevel(serviceLevel);
        validateWeight(weightKg);
        this.zone = zone;
        this.weightKg = weightKg;
        this.serviceLevel = serviceLevel;
    }

    public ShipmentRequest(String zone, double weightKg, ServiceLevel serviceLevel) {
        this(zone, toFiniteBigDecimal(weightKg), serviceLevel);
    }

    public String getZone() {
        return zone;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public ServiceLevel getServiceLevel() {
        return serviceLevel;
    }

    private static BigDecimal toFiniteBigDecimal(double weightKg) {
        if (Double.isNaN(weightKg) || Double.isInfinite(weightKg)) {
            throw new InvalidWeightException(weightKg);
        }
        return BigDecimal.valueOf(weightKg);
    }

    private static void validateZone(String zone) {
        if (zone == null || zone.isBlank()) {
            throw new InvalidShipmentException("Zone is required");
        }
    }

    private static void validateServiceLevel(ServiceLevel serviceLevel) {
        if (serviceLevel == null) {
            throw new InvalidShipmentException("Service level is required");
        }
    }

    private static void validateWeight(BigDecimal weightKg) {
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidWeightException(weightKg);
        }
    }
}
