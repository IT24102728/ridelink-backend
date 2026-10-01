package com.ridelink.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable ID from Ride Management Service */
    @Column(name = "ride_id", nullable = false, unique = true)
    private Long rideId;

    /** Stable ID from Account Service */
    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    /** Stable ID from Driver Service */
    @Column(name = "driver_id")
    private Long driverId;

    @Column(nullable = false)
    private Double distanceKm;

    @Column(nullable = false)
    private Integer waitingMinutes;

    @Column(nullable = false)
    private Double baseFare;

    @Column(nullable = false)
    private Double distanceCharge;

    @Column(nullable = false)
    private Double waitingCharge;

    @Column(nullable = false)
    private Double serviceFee;

    @Column(nullable = false)
    private Double totalFare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String transactionRef;

    private String failureReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}