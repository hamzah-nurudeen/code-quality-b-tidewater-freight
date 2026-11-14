package com.tidewater.freight;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryRateRepositoryTest {

    private InMemoryRateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRateRepository();
    }

    @Test
    void domesticHasRateFiveAndNoFee() {
        ZoneRate zoneRate = repository.findRate("DOMESTIC");

        assertEquals("DOMESTIC", zoneRate.getZone());
        assertEquals(new BigDecimal("5"), zoneRate.getRatePerKg());
        assertEquals(new BigDecimal("0"), zoneRate.getFlatFee());
    }

    @Test
    void regionalHasRateEightAndFeeTen() {
        ZoneRate zoneRate = repository.findRate("REGIONAL");

        assertEquals("REGIONAL", zoneRate.getZone());
        assertEquals(new BigDecimal("8"), zoneRate.getRatePerKg());
        assertEquals(new BigDecimal("10"), zoneRate.getFlatFee());
    }

    @Test
    void internationalHasRateFifteenAndFeeTwentyFive() {
        ZoneRate zoneRate = repository.findRate("INTERNATIONAL");

        assertEquals("INTERNATIONAL", zoneRate.getZone());
        assertEquals(new BigDecimal("15"), zoneRate.getRatePerKg());
        assertEquals(new BigDecimal("25"), zoneRate.getFlatFee());
    }

    @Test
    void moonIsAnUnknownZone() {
        UnknownZoneException e = assertThrows(UnknownZoneException.class,
                () -> repository.findRate("MOON"));

        assertEquals("MOON", e.getZone());
        assertEquals("Unknown zone: MOON", e.getMessage());
    }

    @Test
    void lowercaseZoneIsUnknown() {
        UnknownZoneException e = assertThrows(UnknownZoneException.class,
                () -> repository.findRate("domestic"));

        assertEquals("domestic", e.getZone());
    }

    @Test
    void nullZoneIsUnknown() {
        UnknownZoneException e = assertThrows(UnknownZoneException.class,
                () -> repository.findRate(null));

        assertEquals("Unknown zone: null", e.getMessage());
    }
}
