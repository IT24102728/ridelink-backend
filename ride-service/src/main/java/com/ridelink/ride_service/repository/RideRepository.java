package com.ridelink.ride_service.repository;

import com.ridelink.ride_service.entity.Ride;
import com.ridelink.ride_service.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByPassengerId(Long passengerId);
    List<Ride> findByDriverId(Long driverId);
    List<Ride> findByStatus(RideStatus status);
}