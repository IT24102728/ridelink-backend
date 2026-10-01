package com.ridelink.payment_service.service;

import com.ridelink.payment_service.dto.FinalFareRequestDTO;
import com.ridelink.payment_service.dto.PaymentRequestDTO;
import com.ridelink.payment_service.dto.PaymentResponseDTO;
import com.ridelink.payment_service.entity.Payment;
import com.ridelink.payment_service.entity.PaymentStatus;
import com.ridelink.payment_service.exception.ResourceNotFoundException;
import com.ridelink.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;

    private FareService fareService;
    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        fareService = new FareService();
        ReflectionTestUtils.setField(fareService, "baseFare", 300.0);
        ReflectionTestUtils.setField(fareService, "ratePerKm", 100.0);
        ReflectionTestUtils.setField(fareService, "waitingChargePerMin", 5.0);
        ReflectionTestUtils.setField(fareService, "serviceFeePercent", 2.0);
        paymentService = new PaymentService(paymentRepository, fareService);
    }

    @Test
    void createFinalFare_success() {
        FinalFareRequestDTO req = new FinalFareRequestDTO();
        req.setRideId(1L);
        req.setPassengerId(10L);
        req.setDriverId(20L);
        req.setDistanceKm(10.0);
        req.setWaitingMinutes(5);

        when(paymentRepository.findByRideId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        PaymentResponseDTO res = paymentService.createFinalFare(req);
        assertEquals(PaymentStatus.PENDING, res.getStatus());
        assertEquals(1351.50, res.getTotalFare(), 0.01);
    }

    @Test
    void createFinalFare_duplicate_throws() {
        when(paymentRepository.findByRideId(1L)).thenReturn(Optional.of(new Payment()));
        FinalFareRequestDTO req = new FinalFareRequestDTO();
        req.setRideId(1L);
        req.setPassengerId(10L);
        req.setDistanceKm(5.0);
        assertThrows(IllegalStateException.class, () -> paymentService.createFinalFare(req));
    }

    @Test
    void processPayment_success() {
        Payment p = Payment.builder().id(1L).rideId(1L).passengerId(10L)
                .status(PaymentStatus.PENDING).totalFare(1351.50).build();
        when(paymentRepository.findByRideId(1L)).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequestDTO req = new PaymentRequestDTO();
        req.setRideId(1L);
        req.setPassengerId(10L);
        req.setSimulateSuccess(true);

        PaymentResponseDTO res = paymentService.processPayment(req);
        assertEquals(PaymentStatus.COMPLETED, res.getStatus());
        assertNotNull(res.getTransactionRef());
    }

    @Test
    void processPayment_failure() {
        Payment p = Payment.builder().id(1L).rideId(1L).passengerId(10L)
                .status(PaymentStatus.PENDING).totalFare(1351.50).build();
        when(paymentRepository.findByRideId(1L)).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRequestDTO req = new PaymentRequestDTO();
        req.setRideId(1L);
        req.setPassengerId(10L);
        req.setSimulateSuccess(false);

        PaymentResponseDTO res = paymentService.processPayment(req);
        assertEquals(PaymentStatus.FAILED, res.getStatus());
        assertNotNull(res.getFailureReason());
    }

    @Test
    void getPayment_notFound_throws() {
        when(paymentRepository.findByRideId(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentByRideId(999L));
    }

    @Test
    void getReceipt_pendingPayment_throws() {
        Payment p = Payment.builder().id(1L).rideId(1L).status(PaymentStatus.PENDING).build();
        when(paymentRepository.findByRideId(1L)).thenReturn(Optional.of(p));
        assertThrows(IllegalStateException.class, () -> paymentService.getReceipt(1L));
    }
}