package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;

import java.math.BigDecimal;

// Lists the allowed service levels and the price multiplier each one applies.
public enum ServiceLevel {
    STANDARD(new BigDecimal("1.0")),
    EXPRESS(new BigDecimal("1.5"));

    private final BigDecimal multiplier;

    ServiceLevel(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public static ServiceLevel fromText(String text) {
        if (text == null) {
            throw new InvalidShipmentException("Service level is required");
        }
        for (ServiceLevel level : values()) {
            if (level.name().equals(text)) {
                return level;
            }
        }
        throw new InvalidShipmentException("Unknown service level: " + text);
    }
}
