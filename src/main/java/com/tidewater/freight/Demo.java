package com.tidewater.freight;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.pricing.DefaultPricingRules;
import com.tidewater.freight.rate.InMemoryRateRepository;

import java.math.BigDecimal;

// Runs the calculator on sample shipments and shows that an unknown zone is reported, not priced at 0.
public final class Demo {

    private static final String CURRENCY = "GHS";

    private Demo() {
    }

    public static void main(String[] args) {
        ShippingCalculator calculator =
                new ShippingCalculator(new InMemoryRateRepository(), DefaultPricingRules.create());

        printQuote(calculator, new ShipmentRequest("DOMESTIC", 10, ServiceLevel.STANDARD));
        printQuote(calculator, new ShipmentRequest("INTERNATIONAL", 40, ServiceLevel.EXPRESS));
        printQuote(calculator, new ShipmentRequest("MOON", 10, ServiceLevel.STANDARD));
    }

    private static void printQuote(ShippingCalculator calculator, ShipmentRequest request) {
        String label = "Result " + request.getZone() + " " + (int) request.getWeightKg() + "kg " + request.getServiceLevel();
        try {
            BigDecimal cost = calculator.calculateCost(request);
            System.out.println(label + " -> " + CURRENCY + " " + cost);
        } catch (UnknownZoneException e) {
            System.out.println(label + " -> cannot price shipment: " + e.getMessage());
        }
    }
}
