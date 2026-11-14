package com.tidewater.freight;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceLevelRuleTest {

    private static final BigDecimal STARTING_COST = new BigDecimal("100");

    private final ServiceLevelRule rule = new ServiceLevelRule();
    private final ZoneRate zoneRate = new ZoneRate("DOMESTIC", new BigDecimal("5"), BigDecimal.ZERO);

    @Test
    void standardKeepsTheSameCost() {
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 20, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(request, zoneRate, STARTING_COST);

        assertAmount("100", result);
    }

    @Test
    void expressMultipliesCostByOneAndAHalf() {
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 20, ServiceLevel.EXPRESS);

        BigDecimal result = rule.apply(request, zoneRate, STARTING_COST);

        assertAmount("150", result);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }
}
