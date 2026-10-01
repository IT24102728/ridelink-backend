package com.ridelink.payment_service.dto;

import com.ridelink.payment_service.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReceiptResponseDTO {
    private Long paymentId;
    private Long rideId;
    private Long passengerId;
    private Long driverId;
    private Double distanceKm;
    private Integer waitingMinutes;
    private Double baseFare;
    private Double distanceCharge;
    private Double waitingCharge;
    private Double serviceFee;
    private Double totalFare;
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime issuedAt;
    private String message;
}