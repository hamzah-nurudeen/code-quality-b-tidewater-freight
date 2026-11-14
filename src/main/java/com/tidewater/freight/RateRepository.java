package com.tidewater.freight;

// Supplies the per-kg rate and flat fee for a delivery zone.
public interface RateRepository {

    // Throws UnknownZoneException if the zone does not exist.
    ZoneRate findRate(String zone);
}
