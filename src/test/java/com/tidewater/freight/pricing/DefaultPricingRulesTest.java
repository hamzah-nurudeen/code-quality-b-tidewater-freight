package com.tidewater.freight.pricing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultPricingRulesTest {

    @Test
    void defaultPricingRulesAreInExpectedOrderWithServiceLevelLast() {
        List<PricingRule> rules = DefaultPricingRules.create();

        assertEquals(4, rules.size());
        assertInstanceOf(BasePriceRule.class, rules.get(0));
        assertInstanceOf(ZoneFeeRule.class, rules.get(1));
        assertInstanceOf(HeavyItemSurchargeRule.class, rules.get(2));
        assertInstanceOf(ServiceLevelRule.class, rules.get(3));
        assertThrows(UnsupportedOperationException.class, () -> rules.add(new BasePriceRule()));
    }
}
