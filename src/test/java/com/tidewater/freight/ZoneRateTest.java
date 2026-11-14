package com.tidewater.freight;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZoneRateTest {

    @Test
    void validZoneRateExposesItsValues() {
        ZoneRate zoneRate = new ZoneRate("REGIONAL", new BigDecimal("8"), new BigDecimal("10"));

        assertEquals("REGIONAL", zoneRate.getZone());
        assertEquals(new BigDecimal("8"), zoneRate.getRatePerKg());
        assertEquals(new BigDecimal("10"), zoneRate.getFlatFee());
    }

    @Test
    void nullZoneIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate(null, new BigDecimal("8"), new BigDecimal("10")));

        assertEquals("Zone must not be null", e.getMessage());
    }

    @Test
    void nullRateIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate("REGIONAL", null, new BigDecimal("10")));

        assertEquals("Rate per kg must not be null", e.getMessage());
    }

    @Test
    void nullFeeIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate("REGIONAL", new BigDecimal("8"), null));

        assertEquals("Flat fee must not be null", e.getMessage());
    }

    @Test
    void negativeRateIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate("REGIONAL", new BigDecimal("-0.01"), new BigDecimal("10")));

        assertEquals("Rate per kg must not be negative but was: -0.01", e.getMessage());
    }

    @Test
    void negativeFeeIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate("REGIONAL", new BigDecimal("8"), new BigDecimal("-1")));

        assertEquals("Flat fee must not be negative but was: -1", e.getMessage());
    }

    @Test
    void zeroRateAndZeroFeeAreAllowed() {
        ZoneRate zoneRate = new ZoneRate("FREE", BigDecimal.ZERO, BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, zoneRate.getRatePerKg());
        assertEquals(BigDecimal.ZERO, zoneRate.getFlatFee());
    }
}
