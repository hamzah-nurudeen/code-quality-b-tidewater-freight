package com.tidewater.freight.pricing;

import java.util.ArrayList;
import java.util.List;

// Builds the standard, ordered list of pricing rules used in production.
public final class DefaultPricingRules {

    private DefaultPricingRules() {
    }

    /**
     * Returns an unmodifiable list of the standard pricing rules in the required order:
     * {@link BasePriceRule}, {@link ZoneFeeRule}, {@link HeavyItemSurchargeRule},
     * and {@link ServiceLevelRule}.
     * <p>
     * Order matters: the express multiplier must come last so it applies to the zone fee
     * and the heavy-item surcharge too.
     *
     * @return unmodifiable ordered list of default pricing rules
     */
    public static List<PricingRule> create() {
        List<PricingRule> rules = new ArrayList<>();
        rules.add(new BasePriceRule());
        rules.add(new ZoneFeeRule());
        rules.add(new HeavyItemSurchargeRule());
        rules.add(new ServiceLevelRule());
        return List.copyOf(rules);
    }
}
