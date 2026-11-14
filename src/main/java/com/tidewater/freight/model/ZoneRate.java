package com.tidewater.freight.model;

import java.math.BigDecimal;

// Holds the per-kg rate and the flat fee of one zone together, so they cannot drift apart.
public final class ZoneRate {

    private final String zone;
    private final BigDecimal ratePerKg;
    private final BigDecimal flatFee;

    public ZoneRate(String zone, BigDecimal ratePerKg, BigDecimal flatFee) {
        requireValidZone(zone);
        requireNotNegative(ratePerKg, "Rate per kg");
        requireNotNegative(flatFee, "Flat fee");
        this.zone = zone;
        this.ratePerKg = ratePerKg;
        this.flatFee = flatFee;
    }

    public String getZone() {
        return zone;
    }

    public BigDecimal getRatePerKg() {
        return ratePerKg;
    }

    public BigDecimal getFlatFee() {
        return flatFee;
    }

    private static void requireValidZone(String zone) {
        requirePresent(zone, "Zone");
        if (zone.isBlank()) {
            throw new IllegalArgumentException("Zone must not be blank");
        }
    }

    private static void requirePresent(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    private static void requireNotNegative(BigDecimal amount, String name) {
        requirePresent(amount, name);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must not be negative but was: " + amount);
        }
    }
}
