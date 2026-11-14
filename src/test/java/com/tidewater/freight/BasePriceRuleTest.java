package com.tidewater.freight;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasePriceRuleTest {

    private final BasePriceRule rule = new BasePriceRule();

    @Test
    void rateFiveTimesTenKgIsFiftyAndIgnoresStartingCost() {
        ZoneRate zoneRate = new ZoneRate("DOMESTIC", new BigDecimal("5"), BigDecimal.ZERO);
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 10, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(request, zoneRate, new BigDecimal("999"));

        assertAmount("50", result);
    }

    @Test
    void rateEightTimesTwoAndAHalfKgIsTwenty() {
        ZoneRate zoneRate = new ZoneRate("REGIONAL", new BigDecimal("8"), new BigDecimal("10"));
        ShipmentRequest request = new ShipmentRequest("REGIONAL", 2.5, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(request, zoneRate, BigDecimal.ZERO);

        assertAmount("20.0", result);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }
}
