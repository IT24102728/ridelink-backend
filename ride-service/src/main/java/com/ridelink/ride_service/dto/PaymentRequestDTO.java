package com.ridelink.ride_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {
    private Long rideId;
    private Long passengerId;
    private Double amount;
    private String method; // CARD, CASH
}