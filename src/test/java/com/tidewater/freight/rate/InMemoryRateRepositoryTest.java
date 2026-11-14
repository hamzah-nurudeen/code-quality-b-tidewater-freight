package com.tidewater.freight.rate;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryRateRepositoryTest {

    private InMemoryRateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRateRepository();
    }

    @Test
    void domesticHasConfiguredRateAndZeroFee() {
        ZoneRate zoneRate = repository.findRate("DOMESTIC");

        assertEquals("DOMESTIC", zoneRate.getZone());
        assertAmount("5", zoneRate.getRatePerKg());
        assertAmount("0", zoneRate.getFlatFee());
    }

    @Test
    void regionalHasConfiguredRateAndFee() {
        ZoneRate zoneRate = repository.findRate("REGIONAL");

        assertEquals("REGIONAL", zoneRate.getZone());
        assertAmount("8", zoneRate.getRatePerKg());
        assertAmount("10", zoneRate.getFlatFee());
    }

    @Test
    void internationalHasConfiguredRateAndFee() {
        ZoneRate zoneRate = repository.findRate("INTERNATIONAL");

        assertEquals("INTERNATIONAL", zoneRate.getZone());
        assertAmount("15", zoneRate.getRatePerKg());
        assertAmount("25", zoneRate.getFlatFee());
    }

    @Test
    void customRatesMapCanBeSuppliedToConstructor() {
        Map<String, ZoneRate> customRates = new HashMap<>();
        customRates.put("COASTAL", new ZoneRate("COASTAL", new BigDecimal("12"), new BigDecimal("5")));
        InMemoryRateRepository customRepository = new InMemoryRateRepository(customRates);

        ZoneRate zoneRate = customRepository.findRate("COASTAL");

        assertEquals("COASTAL", zoneRate.getZone());
        assertAmount("12", zoneRate.getRatePerKg());
        assertAmount("5", zoneRate.getFlatFee());
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

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }
}
