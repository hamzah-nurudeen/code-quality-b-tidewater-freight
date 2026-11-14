package com.tidewater.freight;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeavyItemSurchargeRuleTest {

    private static final BigDecimal STARTING_COST = new BigDecimal("100");

    private final HeavyItemSurchargeRule rule = new HeavyItemSurchargeRule();
    private final ZoneRate zoneRate = new ZoneRate("DOMESTIC", new BigDecimal("5"), BigDecimal.ZERO);

    @Test
    void justUnderThirtyKgHasNoSurcharge() {
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 29.99, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(request, zoneRate, STARTING_COST);

        assertAmount("100", result);
    }

    @Test
    void exactlyThirtyKgHasNoSurcharge() {
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 30, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(request, zoneRate, STARTING_COST);

        assertAmount("100", result);
    }

    @Test
    void justOverThirtyKgAddsTwenty() {
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 30.01, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(request, zoneRate, STARTING_COST);

        assertAmount("120", result);
    }

    @Test
    void thirtyOneKgAddsTwenty() {
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 31, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(request, zoneRate, STARTING_COST);

        assertAmount("120", result);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }
}
