package com.ridelink.ride_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RideRequestDTO {

    @NotNull
    private Long passengerId;

    @NotBlank
    private String pickupLocation;

    @NotBlank
    private String destination;

    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double distanceKm;

    private String serviceArea; // optional — passed to Driver Service
}