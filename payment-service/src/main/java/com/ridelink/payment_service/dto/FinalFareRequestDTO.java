package com.ridelink.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinalFareRequestDTO {

    @NotNull
    private Long rideId;

    @NotNull
    private Long passengerId;

    private Long driverId;

    @NotNull
    private Double distanceKm;

    private Integer waitingMinutes = 0;
}