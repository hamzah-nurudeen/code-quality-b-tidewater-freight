package com.tidewater.freight.pricing;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Shared BigDecimal value comparison helper for pricing rule unit tests.
final class MoneyAssertions {

    private MoneyAssertions() {
    }

    static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }
}
