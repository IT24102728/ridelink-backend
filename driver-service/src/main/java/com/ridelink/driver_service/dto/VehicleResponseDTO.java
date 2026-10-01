package com.ridelink.driver_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleResponseDTO {
    private Long id;
    private Long driverId;
    private String registrationNumber;
    private String make;
    private String model;
    private Integer year;
    private String color;
    private Integer passengerCapacity;
}