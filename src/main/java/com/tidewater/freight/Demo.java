package com.tidewater.freight;

import com.tidewater.freight.exception.UnknownZoneException;

// Runs the calculator on sample shipments and shows that an unknown zone is reported, not priced at 0.
public class Demo {

    public static void main(String[] args) {
        ShippingCalculator calculator =
                new ShippingCalculator(new InMemoryRateRepository(), DefaultPricingRules.create());

        ShipmentRequest domestic = new ShipmentRequest("DOMESTIC", 10, ServiceLevel.STANDARD);
        System.out.println("Result DOMESTIC 10kg STANDARD       -> GHS " + calculator.calculateCost(domestic));

        ShipmentRequest international = new ShipmentRequest("INTERNATIONAL", 40, ServiceLevel.EXPRESS);
        System.out.println("Result INTERNATIONAL 40kg EXPRESS   -> GHS " + calculator.calculateCost(international));

        try {
            ShipmentRequest moon = new ShipmentRequest("MOON", 10, ServiceLevel.STANDARD);
            System.out.println("Result unknown zone MOON 10kg STD   -> GHS " + calculator.calculateCost(moon));
        } catch (UnknownZoneException e) {
            System.out.println("Result unknown zone MOON 10kg STD   -> cannot price shipment: " + e.getMessage());
        }
    }
}
