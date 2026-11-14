package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.RateUnavailableException;
import com.tidewater.freight.exception.TidewaterException;
import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import com.tidewater.freight.pricing.PricingRule;
import com.tidewater.freight.rate.RateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

// Prices a shipment by looking up its zone rate and running the pricing rules in order.
public final class ShippingCalculator {

    private static final int SCALE = 2;

    private final RateRepository repository;
    private final List<PricingRule> rules;

    public ShippingCalculator(RateRepository repository, List<PricingRule> rules) {
        this.repository = Objects.requireNonNull(repository, "Rate repository is required");
        this.rules = validateAndCopyRules(rules);
    }

    /**
     * Calculates the total shipping cost in GHS for a validated shipment request,
     * applying each configured pricing rule in order and rounding once to 2 decimal places
     * using {@link RoundingMode#HALF_UP}.
     *
     * @param request the validated shipment request to price
     * @return the final shipping cost with scale 2
     * @throws InvalidShipmentException if {@code request} is {@code null}
     * @throws UnknownZoneException     if the shipment's zone does not exist in the repository
     * @throws RateUnavailableException if the repository fails or returns {@code null}
     */
    public BigDecimal calculateCost(ShipmentRequest request) {
        if (request == null) {
            throw new InvalidShipmentException("Request is required");
        }
        ZoneRate zoneRate = lookUpRate(request.getZone());
        BigDecimal cost = BigDecimal.ZERO;
        for (PricingRule rule : rules) {
            cost = rule.apply(request, zoneRate, cost);
        }
        return cost.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private ZoneRate lookUpRate(String zone) {
        ZoneRate zoneRate;
        try {
            zoneRate = repository.findRate(zone);
        } catch (TidewaterException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RateUnavailableException("Could not load rate for zone: " + zone, e);
        }
        if (zoneRate == null) {
            throw new RateUnavailableException("Rate repository returned null for zone: " + zone);
        }
        return zoneRate;
    }

    private static List<PricingRule> validateAndCopyRules(List<PricingRule> rules) {
        Objects.requireNonNull(rules, "Pricing rules are required");
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("At least one pricing rule is required");
        }
        for (PricingRule rule : rules) {
            Objects.requireNonNull(rule, "Pricing rule must not be null");
        }
        return List.copyOf(rules);
    }
}
