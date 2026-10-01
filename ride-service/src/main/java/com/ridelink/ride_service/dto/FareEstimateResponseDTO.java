package com.ridelink.ride_service.dto;

import lombok.Data;

@Data
public class FareEstimateResponseDTO {
    private Long rideId;
    private Double estimatedFare;
    private Double baseFare;
    private Double distanceCharge;
    private Double waitingCharge;
}