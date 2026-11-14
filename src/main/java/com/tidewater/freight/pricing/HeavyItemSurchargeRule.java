package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

// Adds a fixed surcharge for shipments heavier than the heavy-item threshold.
public class HeavyItemSurchargeRule implements PricingRule {

    private static final double HEAVY_THRESHOLD_KG = 30;
    private static final BigDecimal HEAVY_SURCHARGE = new BigDecimal("20");

    @Override
    public BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost) {
        if (request.getWeightKg() > HEAVY_THRESHOLD_KG) {
            return currentCost.add(HEAVY_SURCHARGE);
        }
        return currentCost;
    }
}
