package com.ridelink.driver_service.repository;

import com.ridelink.driver_service.entity.AvailabilityStatus;
import com.ridelink.driver_service.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByAccountId(Long accountId);
    List<Driver> findByAvailability(AvailabilityStatus status);
    List<Driver> findByAvailabilityAndServiceArea(AvailabilityStatus status, String serviceArea);
}