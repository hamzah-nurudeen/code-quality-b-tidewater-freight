package com.tidewater.freight;

import java.math.BigDecimal;

// One pricing step: takes the running cost and returns the new cost, without side effects.
public interface PricingRule {

    BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost);
}
