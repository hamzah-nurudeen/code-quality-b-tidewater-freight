package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceLevelTest {

    @Test
    void fromTextStandardReturnsStandard() {
        ServiceLevel level = ServiceLevel.fromText("STANDARD");

        assertEquals(ServiceLevel.STANDARD, level);
    }

    @Test
    void fromTextExpressReturnsExpress() {
        ServiceLevel level = ServiceLevel.fromText("EXPRESS");

        assertEquals(ServiceLevel.EXPRESS, level);
    }

    @Test
    void fromTextLowercaseExpressIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> ServiceLevel.fromText("express"));

        assertEquals("Unknown service level: express", e.getMessage());
    }

    @Test
    void fromTextOvernightIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> ServiceLevel.fromText("OVERNIGHT"));

        assertEquals("Unknown service level: OVERNIGHT", e.getMessage());
    }

    @Test
    void fromTextEmptyIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> ServiceLevel.fromText(""));

        assertEquals("Unknown service level: ", e.getMessage());
    }

    @Test
    void fromTextNullIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> ServiceLevel.fromText(null));

        assertEquals("Service level is required", e.getMessage());
    }

    @Test
    void standardMultiplierIsOne() {
        BigDecimal multiplier = ServiceLevel.STANDARD.getMultiplier();

        assertEquals(0, new BigDecimal("1.0").compareTo(multiplier));
    }

    @Test
    void expressMultiplierIsOneAndAHalf() {
        BigDecimal multiplier = ServiceLevel.EXPRESS.getMultiplier();

        assertEquals(0, new BigDecimal("1.5").compareTo(multiplier));
    }
}
