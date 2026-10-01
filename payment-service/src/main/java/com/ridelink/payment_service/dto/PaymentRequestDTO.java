package com.ridelink.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    @NotNull
    private Long rideId;

    @NotNull
    private Long passengerId;

    /** Simulated payment method: CARD, CASH, WALLET */
    private String paymentMethod = "CARD";

    /** Simulated outcome flag (default true = success) */
    private Boolean simulateSuccess = true;
}