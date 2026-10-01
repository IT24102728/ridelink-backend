package com.ridelink.driver_service.dto;

import com.ridelink.driver_service.entity.AvailabilityStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverResponseDTO {
    private Long id;
    private Long accountId;
    private String fullName;
    private String licenseNumber;
    private String phoneNumber;
    private AvailabilityStatus availability;
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;
    private VehicleResponseDTO vehicle;
}