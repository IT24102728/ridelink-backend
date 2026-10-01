package com.ridelink.ride_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable ID from Account Service */
    @Column(nullable = false)
    private Long passengerId;

    /** Stable ID from Driver Service — set when assigned */
    private Long driverId;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String destination;

    private Double pickupLatitude;
    private Double pickupLongitude;

    private Double destinationLatitude;
    private Double destinationLongitude;

    private Double distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status = RideStatus.REQUESTED;

    /** Estimated fare at request time (from Fare Service) */
    private Double estimatedFare;

    /** Final fare after completion */
    private Double finalFare;

    /** Payment ID from Fare & Payment Service */
    private Long paymentId;

    private LocalDateTime requestedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    private String cancellationReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestedAt == null) requestedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}