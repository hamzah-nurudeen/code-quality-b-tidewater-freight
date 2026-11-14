package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.tidewater.freight.pricing.MoneyAssertions.assertAmount;

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
}
