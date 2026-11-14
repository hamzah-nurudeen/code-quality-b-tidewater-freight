package com.tidewater.freight;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import com.tidewater.freight.pricing.BasePriceRule;
import com.tidewater.freight.pricing.DefaultPricingRules;
import com.tidewater.freight.pricing.HeavyItemSurchargeRule;
import com.tidewater.freight.pricing.PricingRule;
import com.tidewater.freight.pricing.ServiceLevelRule;
import com.tidewater.freight.pricing.ZoneFeeRule;
import com.tidewater.freight.rate.InMemoryRateRepository;
import com.tidewater.freight.rate.RateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingCalculatorTest {

    private static final ZoneRate DOMESTIC = new ZoneRate("DOMESTIC", new BigDecimal("5"), new BigDecimal("0"));
    private static final ZoneRate REGIONAL = new ZoneRate("REGIONAL", new BigDecimal("8"), new BigDecimal("10"));

    @Mock
    RateRepository repository;

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(repository, DefaultPricingRules.create());
    }

    // End-to-end table: real InMemoryRateRepository data plus the default rules. A new zone is one more row.
    @ParameterizedTest(name = "{0} {1}kg {2} costs {3}")
    @CsvSource({
            "DOMESTIC,      10,    STANDARD, 50.00",
            "REGIONAL,      5,     STANDARD, 50.00",
            "DOMESTIC,      10,    EXPRESS,  75.00",
            "DOMESTIC,      30,    STANDARD, 150.00",
            "DOMESTIC,      31,    STANDARD, 175.00",
            "REGIONAL,      31,    EXPRESS,  417.00",
            "INTERNATIONAL, 40,    EXPRESS,  967.50",
            "DOMESTIC,      0.333, STANDARD, 1.67"
    })
    void costTableMatchesReferenceValues(String zone, double weightKg, String level, String expectedCost) {
        ShippingCalculator realCalculator =
                new ShippingCalculator(new InMemoryRateRepository(), DefaultPricingRules.create());
        ShipmentRequest request = new ShipmentRequest(zone, weightKg, ServiceLevel.fromText(level));

        BigDecimal result = realCalculator.calculateCost(request);

        assertEquals(new BigDecimal(expectedCost), result);
    }

    @Test
    void costIsRoundedHalfUpToTwoDecimals() {
        when(repository.findRate("DOMESTIC")).thenReturn(DOMESTIC);

        BigDecimal result = calculator.calculateCost(new ShipmentRequest("DOMESTIC", 0.333, ServiceLevel.STANDARD));

        assertEquals(new BigDecimal("1.67"), result);
        verify(repository).findRate("DOMESTIC");
    }

    @Test
    void resultAlwaysHasScaleTwo() {
        when(repository.findRate("DOMESTIC")).thenReturn(DOMESTIC);

        BigDecimal result = calculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, ServiceLevel.STANDARD));

        assertEquals(2, result.scale());
    }

    @Test
    void ruleOrderMattersExpressMustComeAfterFee() {
        when(repository.findRate("REGIONAL")).thenReturn(REGIONAL);
        ShipmentRequest request = new ShipmentRequest("REGIONAL", 5, ServiceLevel.EXPRESS);
        ShippingCalculator wrongOrderCalculator = new ShippingCalculator(repository, serviceLevelBeforeFeeRules());

        BigDecimal defaultOrder = calculator.calculateCost(request);
        BigDecimal wrongOrder = wrongOrderCalculator.calculateCost(request);

        assertEquals(new BigDecimal("75.00"), defaultOrder);
        assertEquals(new BigDecimal("70.00"), wrongOrder);
    }

    @Test
    void defaultPricingRulesAreInExpectedOrderWithServiceLevelLast() {
        List<PricingRule> rules = DefaultPricingRules.create();

        assertEquals(4, rules.size());
        assertTrue(rules.get(0) instanceof BasePriceRule);
        assertTrue(rules.get(1) instanceof ZoneFeeRule);
        assertTrue(rules.get(2) instanceof HeavyItemSurchargeRule);
        assertTrue(rules.get(3) instanceof ServiceLevelRule);
        assertThrows(UnsupportedOperationException.class, () -> rules.add(new BasePriceRule()));
    }

    @Test
    void modifyingInputRuleListAfterConstructionDoesNotAffectCalculator() {
        when(repository.findRate("DOMESTIC")).thenReturn(DOMESTIC);
        List<PricingRule> mutableRules = new ArrayList<>(DefaultPricingRules.create());
        ShippingCalculator isolatedCalculator = new ShippingCalculator(repository, mutableRules);

        mutableRules.clear();
        BigDecimal result = isolatedCalculator.calculateCost(new ShipmentRequest("DOMESTIC", 10, ServiceLevel.STANDARD));

        assertEquals(new BigDecimal("50.00"), result);
    }

    private static List<PricingRule> serviceLevelBeforeFeeRules() {
        List<PricingRule> rules = new ArrayList<>();
        rules.add(new BasePriceRule());
        rules.add(new ServiceLevelRule());
        rules.add(new ZoneFeeRule());
        rules.add(new HeavyItemSurchargeRule());
        return rules;
    }
}
