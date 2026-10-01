package com.ridelink.driver_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleRequestDTO {

    @NotNull
    private Long driverId;

    @NotBlank
    private String registrationNumber;

    @NotBlank
    private String make;

    @NotBlank
    private String model;

    private Integer year;
    private String color;

    @Min(1) @Max(20)
    private Integer passengerCapacity;
}