package com.tidewater.freight;

import com.tidewater.freight.exception.UnknownZoneException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// Keeps the zone rates in memory; a new zone is one more addZone line in the constructor.
public class InMemoryRateRepository implements RateRepository {

    private final Map<String, ZoneRate> rates = new HashMap<>();

    public InMemoryRateRepository() {
        addZone("DOMESTIC", "5", "0");
        addZone("REGIONAL", "8", "10");
        addZone("INTERNATIONAL", "15", "25");
    }

    @Override
    public ZoneRate findRate(String zone) {
        ZoneRate zoneRate = rates.get(zone);
        if (zoneRate == null) {
            throw new UnknownZoneException(zone);
        }
        return zoneRate;
    }

    private void addZone(String zone, String ratePerKg, String flatFee) {
        rates.put(zone, new ZoneRate(zone, new BigDecimal(ratePerKg), new BigDecimal(flatFee)));
    }
}
