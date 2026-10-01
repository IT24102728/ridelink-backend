package com.ridelink.ride_service.dto;

import lombok.Data;

@Data
public class DriverDTO {
    private Long id;
    private Long accountId;
    private String fullName;
    private String licenseNumber;
    private String phoneNumber;
    private String availability;
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;
}