package com.tidewater.freight;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

// Test double: a pricing rule that always fails, used to prove rule failures reach the caller.
class FailingPricingRule implements PricingRule {

    @Override
    public BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost) {
        throw new IllegalStateException("boom");
    }
}
