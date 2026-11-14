package com.tidewater.freight;

import java.math.BigDecimal;

// Adds the zone's flat fee to the running cost.
public class ZoneFeeRule implements PricingRule {

    @Override
    public BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost) {
        return currentCost.add(zoneRate.getFlatFee());
    }
}
