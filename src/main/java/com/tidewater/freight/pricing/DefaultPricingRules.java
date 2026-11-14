package com.tidewater.freight.pricing;

import java.util.ArrayList;
import java.util.List;

// Builds the standard, ordered list of pricing rules used in production.
public class DefaultPricingRules {

    private DefaultPricingRules() {
    }

    // Order matters. The express multiplier must come last so it applies to the fee and the surcharge too.
    public static List<PricingRule> create() {
        List<PricingRule> rules = new ArrayList<>();
        rules.add(new BasePriceRule());
        rules.add(new ZoneFeeRule());
        rules.add(new HeavyItemSurchargeRule());
        rules.add(new ServiceLevelRule());
        return rules;
    }
}
