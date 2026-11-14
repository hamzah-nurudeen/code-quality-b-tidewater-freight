package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;
import com.tidewater.freight.exception.InvalidWeightException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentRequestTest {

    private static final String WEIGHT_MESSAGE = "Weight must be a positive finite number but was: ";

    @Test
    void validRequestExposesItsValues() {
        ShipmentRequest request = new ShipmentRequest("REGIONAL", 12.5, ServiceLevel.EXPRESS);

        assertEquals("REGIONAL", request.getZone());
        assertEquals(0, new BigDecimal("12.5").compareTo(request.getWeightKg()));
        assertEquals(ServiceLevel.EXPRESS, request.getServiceLevel());
    }

    @Test
    void nullZoneIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest(null, 10, ServiceLevel.STANDARD));

        assertEquals("Zone is required", e.getMessage());
    }

    @Test
    void blankZoneIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest("   ", 10, ServiceLevel.STANDARD));

        assertEquals("Zone is required", e.getMessage());
    }

    @Test
    void emptyZoneIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest("", 10, ServiceLevel.STANDARD));

        assertEquals("Zone is required", e.getMessage());
    }

    @Test
    void nullServiceLevelIsRejected() {
        InvalidShipmentException e = assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest("DOMESTIC", 10, null));

        assertEquals("Service level is required", e.getMessage());
    }

    @Test
    void negativeWeightIsRejected() {
        InvalidWeightException e = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", -1, ServiceLevel.STANDARD));

        assertEquals(WEIGHT_MESSAGE + "-1.0", e.getMessage());
    }

    @Test
    void zeroWeightIsRejected() {
        InvalidWeightException e = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", 0, ServiceLevel.STANDARD));

        assertEquals(WEIGHT_MESSAGE + "0.0", e.getMessage());
    }

    @Test
    void nullBigDecimalWeightIsRejected() {
        InvalidWeightException e = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", null, ServiceLevel.STANDARD));

        assertEquals(WEIGHT_MESSAGE + "null", e.getMessage());
    }

    @Test
    void zeroBigDecimalWeightIsRejected() {
        InvalidWeightException e = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", BigDecimal.ZERO, ServiceLevel.STANDARD));

        assertEquals(WEIGHT_MESSAGE + "0", e.getMessage());
    }

    @Test
    void notANumberWeightIsRejected() {
        InvalidWeightException e = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", Double.NaN, ServiceLevel.STANDARD));

        assertEquals(WEIGHT_MESSAGE + "NaN", e.getMessage());
    }

    @Test
    void positiveInfiniteWeightIsRejected() {
        InvalidWeightException e = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", Double.POSITIVE_INFINITY, ServiceLevel.STANDARD));

        assertEquals(WEIGHT_MESSAGE + "Infinity", e.getMessage());
    }

    @Test
    void negativeInfiniteWeightIsRejected() {
        InvalidWeightException e = assertThrows(InvalidWeightException.class,
                () -> new ShipmentRequest("DOMESTIC", Double.NEGATIVE_INFINITY, ServiceLevel.STANDARD));

        assertEquals(WEIGHT_MESSAGE + "-Infinity", e.getMessage());
    }

    @Test
    void verySmallPositiveWeightIsAccepted() {
        ShipmentRequest request = new ShipmentRequest("DOMESTIC", new BigDecimal("0.001"), ServiceLevel.STANDARD);

        assertEquals(0, new BigDecimal("0.001").compareTo(request.getWeightKg()));
    }

    @Test
    void zoneIsKeptExactlyAsGiven() {
        ShipmentRequest request = new ShipmentRequest(" domestic ", 10, ServiceLevel.STANDARD);

        assertEquals(" domestic ", request.getZone());
    }
}
