package com.ridelink.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class FareEstimateRequestDTO {

    @NotNull(message = "distanceKm is required")
    @PositiveOrZero(message = "distanceKm must be >= 0")
    private Double distanceKm;

    @PositiveOrZero(message = "waitingMinutes must be >= 0")
    private Integer waitingMinutes = 0;

    private String pickupLocation;
    private String destination;
}