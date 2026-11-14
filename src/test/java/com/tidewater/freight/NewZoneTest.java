package com.tidewater.freight;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import com.tidewater.freight.pricing.DefaultPricingRules;
import com.tidewater.freight.rate.RateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// No production class was edited to support this zone. That is the Open/Closed proof.
@ExtendWith(MockitoExtension.class)
class NewZoneTest {

    private static final ZoneRate ARCTIC = new ZoneRate("ARCTIC", new BigDecimal("20"), new BigDecimal("40"));

    @Mock
    RateRepository repository;

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(repository, DefaultPricingRules.create());
    }

    @Test
    void arcticTenKgStandardCosts240() {
        when(repository.findRate("ARCTIC")).thenReturn(ARCTIC);

        BigDecimal result = calculator.calculateCost(new ShipmentRequest("ARCTIC", 10, ServiceLevel.STANDARD));

        assertEquals(new BigDecimal("240.00"), result);
    }

    @Test
    void arcticAtExactHeavyBoundary30KgStandardHasNoSurcharge() {
        when(repository.findRate("ARCTIC")).thenReturn(ARCTIC);

        BigDecimal result = calculator.calculateCost(new ShipmentRequest("ARCTIC", 30, ServiceLevel.STANDARD));

        assertEquals(new BigDecimal("640.00"), result);
    }

    @Test
    void arcticThirtyFiveKgExpressCosts1140() {
        when(repository.findRate("ARCTIC")).thenReturn(ARCTIC);

        BigDecimal result = calculator.calculateCost(new ShipmentRequest("ARCTIC", 35, ServiceLevel.EXPRESS));

        assertEquals(new BigDecimal("1140.00"), result);
    }
}
