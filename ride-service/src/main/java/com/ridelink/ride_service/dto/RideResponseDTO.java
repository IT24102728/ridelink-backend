package com.ridelink.ride_service.dto;

import com.ridelink.ride_service.entity.RideStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RideResponseDTO {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String pickupLocation;
    private String destination;
    private Double distanceKm;
    private RideStatus status;
    private Double estimatedFare;
    private Double finalFare;
    private Long paymentId;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
}