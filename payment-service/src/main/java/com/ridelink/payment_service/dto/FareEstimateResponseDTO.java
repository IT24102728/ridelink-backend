package com.ridelink.payment_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FareEstimateResponseDTO {
    private Double distanceKm;
    private Integer waitingMinutes;
    private Double baseFare;
    private Double distanceCharge;
    private Double waitingCharge;
    private Double serviceFee;
    private Double totalFare;
    private String rule;
}