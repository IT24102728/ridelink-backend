package com.ridelink.driver_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverRequestDTO {

    @NotNull(message = "accountId is required")
    private Long accountId;

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "licenseNumber is required")
    private String licenseNumber;

    private String phoneNumber;
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;
}