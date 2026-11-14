package com.tidewater.freight;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.InvalidWeightException;
import com.tidewater.freight.exception.RateUnavailableException;
import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingCalculatorFailureTest {

    private static final ZoneRate DOMESTIC = new ZoneRate("DOMESTIC", new BigDecimal("5"), new BigDecimal("0"));

    @Mock
    RateRepository repository;

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(repository, DefaultPricingRules.create());
    }

    @Test
    void unknownZoneIsReportedAndNotWrapped() {
        UnknownZoneException unknownZone = new UnknownZoneException("MOON");
        when(repository.findRate("MOON")).thenThrow(unknownZone);
        ShipmentRequest request = new ShipmentRequest("MOON", 10, ServiceLevel.STANDARD);

        RuntimeException thrown = assertThrows(UnknownZoneException.class, () -> calculator.calculateCost(request));

        assertEquals("Unknown zone: MOON", thrown.getMessage());
        assertFalse(thrown instanceof RateUnavailableException);
        assertSame(unknownZone, thrown);
    }

    @Test
    void repositoryFailureIsWrappedWithOriginalCause() {
        IllegalStateException databaseDown = new IllegalStateException("db down");
        when(repository.findRate("DOMESTIC")).thenThrow(databaseDown);
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 10, ServiceLevel.STANDARD);

        RateUnavailableException thrown =
                assertThrows(RateUnavailableException.class, () -> calculator.calculateCost(request));

        assertSame(databaseDown, thrown.getCause());
        assertEquals("Could not load rate for zone: DOMESTIC", thrown.getMessage());
    }

    @Test
    void nullRequestIsRejected() {
        InvalidShipmentException thrown =
                assertThrows(InvalidShipmentException.class, () -> calculator.calculateCost(null));

        assertEquals("Request is required", thrown.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void nullRepositoryIsRejected() {
        List<PricingRule> rules = DefaultPricingRules.create();

        NullPointerException thrown =
                assertThrows(NullPointerException.class, () -> new ShippingCalculator(null, rules));

        assertEquals("Rate repository is required", thrown.getMessage());
    }

    @Test
    void nullRuleListIsRejected() {
        NullPointerException thrown =
                assertThrows(NullPointerException.class, () -> new ShippingCalculator(repository, null));

        assertEquals("Pricing rules are required", thrown.getMessage());
    }

    @Test
    void failingRuleReachesCallerInsteadOfZero() {
        when(repository.findRate("DOMESTIC")).thenReturn(DOMESTIC);
        List<PricingRule> rules = new ArrayList<>();
        rules.add(new BasePriceRule());
        rules.add(new FailingPricingRule());
        ShippingCalculator failingCalculator = new ShippingCalculator(repository, rules);
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", 10, ServiceLevel.STANDARD);

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> failingCalculator.calculateCost(request));

        assertEquals("boom", thrown.getMessage());
    }

    @Test
    void invalidWeightIsRejectedBeforeRepositoryIsUsed() {
        InvalidWeightException thrown = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", -5, ServiceLevel.STANDARD));

        assertEquals("Weight must be a positive finite number but was: -5.0", thrown.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void unknownZoneNeverReturnsAFreeQuote() {
        // Regression guard for the original catch-all that returned 0 for an unknown zone.
        // assertThrows fails if calculateCost returns any number at all, including 0.
        ShippingCalculator realCalculator =
                new ShippingCalculator(new InMemoryRateRepository(), DefaultPricingRules.create());
        ShipmentRequest request = new ShipmentRequest("MOON", 10, ServiceLevel.STANDARD);

        UnknownZoneException thrown =
                assertThrows(UnknownZoneException.class, () -> realCalculator.calculateCost(request));

        assertEquals("MOON", thrown.getZone());
    }
}
