package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

// One pricing step: takes the running cost and returns the new cost, without side effects.
public interface PricingRule {

    /**
     * Applies this pricing step to the running cost of a shipment without mutating its inputs.
     *
     * @param request     the validated shipment request
     * @param zoneRate    the rate and flat fee for the shipment's destination zone
     * @param currentCost the accumulated cost from previous rules
     * @return the updated shipping cost after applying this rule
     */
    BigDecimal apply(ShipmentRequest request, ZoneRate zoneRate, BigDecimal currentCost);
}
