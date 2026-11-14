package com.tidewater.freight;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

// Starts the price: the zone's rate per kg times the shipment weight.
public class BasePriceRule implements PricingRule {

    @Override
    public BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost) {
        BigDecimal weight = BigDecimal.valueOf(request.getWeightKg());
        return zoneRate.getRatePerKg().multiply(weight);
    }
}
