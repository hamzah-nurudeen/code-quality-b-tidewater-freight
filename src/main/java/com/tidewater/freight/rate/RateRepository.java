package com.tidewater.freight.rate;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;

// Supplies the per-kg rate and flat fee for a delivery zone.
public interface RateRepository {

    /**
     * Looks up the rate and flat fee for the given destination zone (exact, case-sensitive match).
     *
     * @param zone the destination zone identifier
     * @return the non-null {@link ZoneRate} for the zone
     * @throws UnknownZoneException if the zone does not exist
     */
    ZoneRate findRate(String zone);
}
