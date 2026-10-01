package com.ridelink.payment_service.controller;

import com.ridelink.payment_service.dto.*;
import com.ridelink.payment_service.service.FareService;
import com.ridelink.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Fare estimation, payment processing and receipts")
public class PaymentController {

    private final PaymentService paymentService;
    private final FareService fareService;

    // ---------- FARE ESTIMATION ----------

    @PostMapping("/estimate")
    @Operation(summary = "Estimate fare (public, documented rule)")
    public ResponseEntity<FareEstimateResponseDTO> estimate(@Valid @RequestBody FareEstimateRequestDTO dto) {
        return ResponseEntity.ok(fareService.estimate(dto));
    }

    // ---------- INTERSERVICE ----------

    @PostMapping("/final-fare")
    @Operation(summary = "Calculate final fare and create PENDING payment (used by Ride Service)")
    public ResponseEntity<PaymentResponseDTO> createFinalFare(@Valid @RequestBody FinalFareRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createFinalFare(dto));
    }

    // ---------- PAYMENT ----------

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @Operation(summary = "Process a simulated payment")
    public ResponseEntity<PaymentResponseDTO> process(@Valid @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.ok(paymentService.processPayment(dto));
    }

    @PostMapping("/create-and-pay")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @Operation(summary = "One-shot: create final fare + process payment")
    public ResponseEntity<PaymentResponseDTO> createAndPay(@Valid @RequestBody FinalFareRequestDTO dto,
                                                           @RequestParam(defaultValue = "true") boolean simulateSuccess) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createAndPay(dto, simulateSuccess));
    }

    // ---------- RETRIEVAL ----------

    @GetMapping("/ride/{rideId}")
    @Operation(summary = "Get payment record by rideId")
    public ResponseEntity<PaymentResponseDTO> getByRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(paymentService.getPaymentByRideId(rideId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "List payments for a passenger")
    public ResponseEntity<List<PaymentResponseDTO>> getByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByPassenger(passengerId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all payments (admin)")
    public ResponseEntity<List<PaymentResponseDTO>> getAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // ---------- RECEIPT ----------

    @GetMapping("/receipt/{rideId}")
    @Operation(summary = "Get receipt for a completed ride")
    public ResponseEntity<ReceiptResponseDTO> getReceipt(@PathVariable Long rideId) {
        return ResponseEntity.ok(paymentService.getReceipt(rideId));
    }

    // ---------- REFUND (admin) ----------

    @PostMapping("/refund/{rideId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a completed payment (admin)")
    public ResponseEntity<PaymentResponseDTO> refund(@PathVariable Long rideId) {
        return ResponseEntity.ok(paymentService.refund(rideId));
    }
}