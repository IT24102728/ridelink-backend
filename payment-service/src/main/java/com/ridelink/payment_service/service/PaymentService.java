package com.ridelink.payment_service.service;

import com.ridelink.payment_service.dto.*;
import com.ridelink.payment_service.entity.Payment;
import com.ridelink.payment_service.entity.PaymentStatus;
import com.ridelink.payment_service.exception.ResourceNotFoundException;
import com.ridelink.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FareService fareService;

    /**
     * INTERSERVICE endpoint (called by Ride Management Service on ride completion).
     * Calculates the final fare and creates a PENDING payment record.
     */
    @Transactional
    public PaymentResponseDTO createFinalFare(FinalFareRequestDTO req) {
        paymentRepository.findByRideId(req.getRideId()).ifPresent(p -> {
            throw new IllegalStateException("Payment already exists for rideId: " + req.getRideId());
        });

        int waiting = req.getWaitingMinutes() == null ? 0 : req.getWaitingMinutes();
        double distance = req.getDistanceKm();

        double distanceCharge = distance * fareService.getRatePerKm();
        double waitingCharge = waiting * fareService.getWaitingChargePerMin();
        double subtotal = fareService.getBaseFare() + distanceCharge + waitingCharge;
        double serviceFee = subtotal * fareService.getServiceFeePercent() / 100.0;
        double total = round(subtotal + serviceFee);

        Payment payment = Payment.builder()
                .rideId(req.getRideId())
                .passengerId(req.getPassengerId())
                .driverId(req.getDriverId())
                .distanceKm(distance)
                .waitingMinutes(waiting)
                .baseFare(fareService.getBaseFare())
                .distanceCharge(round(distanceCharge))
                .waitingCharge(round(waitingCharge))
                .serviceFee(round(serviceFee))
                .totalFare(total)
                .status(PaymentStatus.PENDING)
                .build();

        return toDTO(paymentRepository.save(payment));
    }

    /** Process a simulated payment */
    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO req) {
        Payment payment = paymentRepository.findByRideId(req.getRideId())
                .orElseThrow(() -> new ResourceNotFoundException("No payment record for rideId: " + req.getRideId()));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment already completed for rideId: " + req.getRideId());
        }

        boolean success = req.getSimulateSuccess() == null || req.getSimulateSuccess();

        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setFailureReason(null);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment failure");
        }

        return toDTO(paymentRepository.save(payment));
    }

    /** Legacy/manual: create + immediately pay in one shot */
    @Transactional
    public PaymentResponseDTO createAndPay(FinalFareRequestDTO fareReq, boolean simulateSuccess) {
        PaymentResponseDTO created = createFinalFare(fareReq);
        PaymentRequestDTO pay = new PaymentRequestDTO();
        pay.setRideId(created.getRideId());
        pay.setPassengerId(created.getPassengerId());
        pay.setSimulateSuccess(simulateSuccess);
        return processPayment(pay);
    }

    public PaymentResponseDTO getPaymentByRideId(Long rideId) {
        Payment p = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for rideId: " + rideId));
        return toDTO(p);
    }

    public PaymentResponseDTO getPaymentById(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return toDTO(p);
    }

    public List<PaymentResponseDTO> getPaymentsByPassenger(Long passengerId) {
        return paymentRepository.findByPassengerId(passengerId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ReceiptResponseDTO getReceipt(Long rideId) {
        Payment p = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for rideId: " + rideId));

        if (p.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Receipt available only for COMPLETED payments. Current: " + p.getStatus());
        }

        return ReceiptResponseDTO.builder()
                .paymentId(p.getId())
                .rideId(p.getRideId())
                .passengerId(p.getPassengerId())
                .driverId(p.getDriverId())
                .distanceKm(p.getDistanceKm())
                .waitingMinutes(p.getWaitingMinutes())
                .baseFare(p.getBaseFare())
                .distanceCharge(p.getDistanceCharge())
                .waitingCharge(p.getWaitingCharge())
                .serviceFee(p.getServiceFee())
                .totalFare(p.getTotalFare())
                .status(p.getStatus())
                .transactionRef(p.getTransactionRef())
                .issuedAt(LocalDateTime.now())
                .message("Thank you for riding with RideLink (simulated)")
                .build();
    }

    @Transactional
    public PaymentResponseDTO refund(Long rideId) {
        Payment p = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for rideId: " + rideId));
        if (p.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }
        p.setStatus(PaymentStatus.REFUNDED);
        return toDTO(paymentRepository.save(p));
    }

    private PaymentResponseDTO toDTO(Payment p) {
        return PaymentResponseDTO.builder()
                .id(p.getId())
                .rideId(p.getRideId())
                .passengerId(p.getPassengerId())
                .driverId(p.getDriverId())
                .totalFare(p.getTotalFare())
                .status(p.getStatus())
                .transactionRef(p.getTransactionRef())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}