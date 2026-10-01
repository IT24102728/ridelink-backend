package com.ridelink.ride_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FareEstimateRequestDTO {
    private Long rideId;
    private Double distanceKm;
    private Integer waitingMinutes;
}