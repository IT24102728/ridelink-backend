package com.ridelink.payment_service.repository;

import com.ridelink.payment_service.entity.Payment;
import com.ridelink.payment_service.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRideId(Long rideId);
    List<Payment> findByPassengerId(Long passengerId);
    List<Payment> findByStatus(PaymentStatus status);
}