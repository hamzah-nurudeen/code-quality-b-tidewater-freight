package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.RateUnavailableException;
import com.tidewater.freight.exception.TidewaterException;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Prices a shipment by looking up its zone rate and running the pricing rules in order.
public class ShippingCalculator {

    private static final int SCALE = 2;

    private final RateRepository repository;
    private final List<PricingRule> rules;

    public ShippingCalculator(RateRepository repository, List<PricingRule> rules) {
        this.repository = Objects.requireNonNull(repository, "Rate repository is required");
        this.rules = new ArrayList<>(Objects.requireNonNull(rules, "Pricing rules are required"));
    }

    public BigDecimal calculateCost(ShipmentRequest request) {
        if (request == null) {
            throw new InvalidShipmentException("Request is required");
        }
        ZoneRate zoneRate = findRateSafely(request.getZone());
        BigDecimal cost = BigDecimal.ZERO;
        for (PricingRule rule : rules) {
            cost = rule.apply(request, zoneRate, cost);
        }
        return cost.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private ZoneRate findRateSafely(String zone) {
        try {
            return repository.findRate(zone);
        } catch (TidewaterException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RateUnavailableException("Could not load rate for zone: " + zone, e);
        }
    }
}
