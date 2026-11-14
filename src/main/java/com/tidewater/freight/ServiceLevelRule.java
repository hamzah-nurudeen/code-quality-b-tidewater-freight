package com.tidewater.freight;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

// Multiplies the running cost by the shipment's service-level multiplier.
public class ServiceLevelRule implements PricingRule {

    @Override
    public BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost) {
        return currentCost.multiply(request.getServiceLevel().getMultiplier());
    }
}
