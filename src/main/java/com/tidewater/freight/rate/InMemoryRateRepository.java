package com.tidewater.freight.rate;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

// Keeps the zone rates in memory, loaded from external configuration or supplied via constructor.
public class InMemoryRateRepository implements RateRepository {

    private static final String DEFAULT_RATES_BUNDLE = "rates";

    private final Map<String, ZoneRate> rates;

    public InMemoryRateRepository() {
        this(loadRatesFromBundle(DEFAULT_RATES_BUNDLE));
    }

    public InMemoryRateRepository(Map<String, ZoneRate> rates) {
        this.rates = new HashMap<>(Objects.requireNonNull(rates, "Rates map is required"));
    }

    @Override
    public ZoneRate findRate(String zone) {
        ZoneRate zoneRate = rates.get(zone);
        if (zoneRate == null) {
            throw new UnknownZoneException(zone);
        }
        return zoneRate;
    }

    private static Map<String, ZoneRate> loadRatesFromBundle(String bundleName) {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
        Map<String, ZoneRate> loaded = new HashMap<>();
        for (String zone : bundle.keySet()) {
            String[] parts = bundle.getString(zone).split(",");
            loaded.put(zone, new ZoneRate(zone, new BigDecimal(parts[0].trim()), new BigDecimal(parts[1].trim())));
        }
        return loaded;
    }
}
