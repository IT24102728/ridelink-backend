package com.ridelink.payment_service.service;

import com.ridelink.payment_service.dto.FareEstimateRequestDTO;
import com.ridelink.payment_service.dto.FareEstimateResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Fare rule (documented):
 *   total = baseFare + (distanceKm × ratePerKm) + (waitingMinutes × waitingChargePerMin)
 *   serviceFee = total × serviceFeePercent / 100
 *   finalTotal = total + serviceFee
 */
@Service
public class FareService {

    @Value("${fare.base-fare}")
    private double baseFare;

    @Value("${fare.rate-per-km}")
    private double ratePerKm;

    @Value("${fare.waiting-charge-per-min}")
    private double waitingChargePerMin;

    @Value("${fare.service-fee-percent}")
    private double serviceFeePercent;

    public FareEstimateResponseDTO estimate(FareEstimateRequestDTO req) {
        double distance = req.getDistanceKm() == null ? 0 : req.getDistanceKm();
        int waiting = req.getWaitingMinutes() == null ? 0 : req.getWaitingMinutes();

        double distanceCharge = distance * ratePerKm;
        double waitingCharge = waiting * waitingChargePerMin;
        double subtotal = baseFare + distanceCharge + waitingCharge;
        double serviceFee = subtotal * serviceFeePercent / 100.0;
        double total = subtotal + serviceFee;

        return FareEstimateResponseDTO.builder()
                .distanceKm(round(distance))
                .waitingMinutes(waiting)
                .baseFare(round(baseFare))
                .distanceCharge(round(distanceCharge))
                .waitingCharge(round(waitingCharge))
                .serviceFee(round(serviceFee))
                .totalFare(round(total))
                .rule("total = baseFare + (distanceKm × ratePerKm) + (waitingMinutes × waitingChargePerMin); " +
                      "serviceFee = subtotal × " + serviceFeePercent + "%; finalTotal = subtotal + serviceFee")
                .build();
    }

    public double calculateTotal(double distanceKm, int waitingMinutes) {
        double distanceCharge = distanceKm * ratePerKm;
        double waitingCharge = waitingMinutes * waitingChargePerMin;
        double subtotal = baseFare + distanceCharge + waitingCharge;
        double serviceFee = subtotal * serviceFeePercent / 100.0;
        return round(subtotal + serviceFee);
    }

    public double getBaseFare() { return baseFare; }
    public double getRatePerKm() { return ratePerKm; }
    public double getWaitingChargePerMin() { return waitingChargePerMin; }
    public double getServiceFeePercent() { return serviceFeePercent; }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}