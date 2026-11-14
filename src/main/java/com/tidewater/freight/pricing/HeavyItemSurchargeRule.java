package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;
import java.util.Objects;

// Adds a configurable surcharge for shipments heavier than the heavy-item threshold.
public class HeavyItemSurchargeRule implements PricingRule {

    private static final BigDecimal DEFAULT_HEAVY_THRESHOLD_KG = new BigDecimal("30");
    private static final BigDecimal DEFAULT_HEAVY_SURCHARGE = new BigDecimal("20");

    private final BigDecimal heavyThresholdKg;
    private final BigDecimal heavySurcharge;

    public HeavyItemSurchargeRule() {
        this(DEFAULT_HEAVY_THRESHOLD_KG, DEFAULT_HEAVY_SURCHARGE);
    }

    public HeavyItemSurchargeRule(BigDecimal heavyThresholdKg, BigDecimal heavySurcharge) {
        this.heavyThresholdKg = Objects.requireNonNull(heavyThresholdKg, "Heavy threshold is required");
        this.heavySurcharge = Objects.requireNonNull(heavySurcharge, "Heavy surcharge is required");
    }

    @Override
    public BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost) {
        if (request.getWeightKg().compareTo(heavyThresholdKg) > 0) {
            return currentCost.add(heavySurcharge);
        }
        return currentCost;
    }
}
