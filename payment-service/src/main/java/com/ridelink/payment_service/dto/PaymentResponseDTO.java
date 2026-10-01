package com.ridelink.payment_service.dto;

import com.ridelink.payment_service.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDTO {
    private Long id;
    private Long rideId;
    private Long passengerId;
    private Long driverId;
    private Double totalFare;
    private PaymentStatus status;
    private String transactionRef;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}