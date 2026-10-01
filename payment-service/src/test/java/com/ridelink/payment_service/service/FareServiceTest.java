package com.ridelink.payment_service.service;

import com.ridelink.payment_service.dto.FareEstimateRequestDTO;
import com.ridelink.payment_service.dto.FareEstimateResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class FareServiceTest {

    private FareService fareService;

    @BeforeEach
    void setup() {
        fareService = new FareService();
        ReflectionTestUtils.setField(fareService, "baseFare", 300.0);
        ReflectionTestUtils.setField(fareService, "ratePerKm", 100.0);
        ReflectionTestUtils.setField(fareService, "waitingChargePerMin", 5.0);
        ReflectionTestUtils.setField(fareService, "serviceFeePercent", 2.0);
    }

    @Test
    void estimate_normalCase() {
        FareEstimateRequestDTO req = new FareEstimateRequestDTO();
        req.setDistanceKm(10.0);
        req.setWaitingMinutes(5);

        FareEstimateResponseDTO res = fareService.estimate(req);
        // 300 + 1000 + 25 = 1325 ; +2% = 1351.50
        assertEquals(1325.0, res.getBaseFare() + res.getDistanceCharge() + res.getWaitingCharge(), 0.01);
        assertEquals(1351.50, res.getTotalFare(), 0.01);
        assertNotNull(res.getRule());
    }

    @Test
    void estimate_zeroDistance() {
        FareEstimateRequestDTO req = new FareEstimateRequestDTO();
        req.setDistanceKm(0.0);
        req.setWaitingMinutes(0);
        FareEstimateResponseDTO res = fareService.estimate(req);
        // 300 + 0 + 0 = 300 ; +2% = 306
        assertEquals(306.0, res.getTotalFare(), 0.01);
    }

    @Test
    void calculateTotal_matchesEstimate() {
        double total = fareService.calculateTotal(10.0, 5);
        assertEquals(1351.50, total, 0.01);
    }
}