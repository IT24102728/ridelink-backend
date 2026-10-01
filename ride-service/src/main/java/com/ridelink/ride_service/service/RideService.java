package com.ridelink.ride_service.service;

import com.ridelink.ride_service.client.DriverServiceClient;
import com.ridelink.ride_service.client.FareServiceClient;
import com.ridelink.ride_service.dto.*;
import com.ridelink.ride_service.entity.Ride;
import com.ridelink.ride_service.entity.RideStatus;
import com.ridelink.ride_service.exception.InvalidStateTransitionException;
import com.ridelink.ride_service.exception.ResourceNotFoundException;
import com.ridelink.ride_service.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FareServiceClient fareServiceClient;

    /** ============ WORKFLOW 1: Create + Estimate + Assign ============ */
    @Transactional
    public RideResponseDTO requestRide(RideRequestDTO dto) {

        // Step 1: create ride in REQUESTED state
        Ride ride = Ride.builder()
                .passengerId(dto.getPassengerId())
                .pickupLocation(dto.getPickupLocation())
                .destination(dto.getDestination())
                .pickupLatitude(dto.getPickupLatitude())
                .pickupLongitude(dto.getPickupLongitude())
                .destinationLatitude(dto.getDestinationLatitude())
                .destinationLongitude(dto.getDestinationLongitude())
                .distanceKm(dto.getDistanceKm())
                .status(RideStatus.REQUESTED)
                .build();
        ride = rideRepository.save(ride);

        // Step 2: interservice call #2 — ask Fare Service for estimate
        FareEstimateRequestDTO fareReq = new FareEstimateRequestDTO(
                ride.getId(), dto.getDistanceKm(), 0);
        FareEstimateResponseDTO fareResp = fareServiceClient.estimateFare(fareReq);
        ride.setEstimatedFare(fareResp.getEstimatedFare());

        // Step 3: interservice call #1 — ask Driver Service for eligible drivers
        List<DriverDTO> eligible = driverServiceClient.getEligibleDrivers(dto.getServiceArea());

        if (eligible.isEmpty()) {
            // NEGATIVE CASE #1 — no available driver
            ride.setStatus(RideStatus.CANCELLED);
            ride.setCancellationReason("No available driver");
            ride.setCancelledAt(LocalDateTime.now());
            rideRepository.save(ride);
            throw new IllegalStateException("No available driver for this area");
        }

        // Step 4: assign nearest/first eligible driver (simple documented rule)
        DriverDTO assigned = eligible.get(0);
        ride.setDriverId(assigned.getId());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(LocalDateTime.now());

        return toDTO(rideRepository.save(ride));
    }

    /** ============ Read ============ */
    public RideResponseDTO getRide(Long id) {
        return toDTO(rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + id)));
    }

    public List<RideResponseDTO> getRidesByPassenger(Long passengerId) {
        return rideRepository.findByPassengerId(passengerId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<RideResponseDTO> getRidesByDriver(Long driverId) {
        return rideRepository.findByDriverId(driverId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** ============ WORKFLOW 2: Lifecycle transitions ============ */
    @Transactional
    public RideResponseDTO acceptRide(Long rideId, Long driverId) {
        Ride ride = findOrThrow(rideId);
        assertStatus(ride, RideStatus.ASSIGNED);
        if (!driverId.equals(ride.getDriverId())) {
            throw new InvalidStateTransitionException("Driver " + driverId + " is not assigned to this ride");
        }
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());
        return toDTO(rideRepository.save(ride));
    }

    @Transactional
    public RideResponseDTO startRide(Long rideId, Long driverId) {
        Ride ride = findOrThrow(rideId);
        assertStatus(ride, RideStatus.ACCEPTED);
        if (!driverId.equals(ride.getDriverId())) {
            throw new InvalidStateTransitionException("Only assigned driver can start ride");
        }
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        return toDTO(rideRepository.save(ride));
    }

    @Transactional
    public RideResponseDTO completeRide(Long rideId, Long driverId) {
        Ride ride = findOrThrow(rideId);
        assertStatus(ride, RideStatus.IN_PROGRESS);
        if (!driverId.equals(ride.getDriverId())) {
            throw new InvalidStateTransitionException("Only assigned driver can complete ride");
        }

        // Interservice call #2 again — get final fare & create payment
        FareEstimateRequestDTO req = new FareEstimateRequestDTO(
                ride.getId(), ride.getDistanceKm(), 0);
        FareEstimateResponseDTO fareResp = fareServiceClient.estimateFare(req);
        ride.setFinalFare(fareResp.getEstimatedFare());

        Long paymentId = fareServiceClient.pay(new PaymentRequestDTO(
                ride.getId(), ride.getPassengerId(), ride.getFinalFare(), "CARD"));
        ride.setPaymentId(paymentId);

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        return toDTO(rideRepository.save(ride));
    }

    @Transactional
    public RideResponseDTO cancelRide(Long rideId, Long requesterId, String reason) {
        Ride ride = findOrThrow(rideId);
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidStateTransitionException("Cannot cancel a completed ride");
        }
        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Ride already cancelled");
        }
        // NEGATIVE CASE #2 — only passenger or assigned driver can cancel
        boolean isPassenger = requesterId.equals(ride.getPassengerId());
        boolean isDriver = requesterId.equals(ride.getDriverId());
        if (!isPassenger && !isDriver) {
            throw new InvalidStateTransitionException("Not authorised to cancel this ride");
        }
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancellationReason(reason == null ? "No reason provided" : reason);
        return toDTO(rideRepository.save(ride));
    }

    /* ============ helpers ============ */

    private Ride findOrThrow(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + id));
    }

    private void assertStatus(Ride ride, RideStatus expected) {
        if (ride.getStatus() != expected) {
            throw new InvalidStateTransitionException(
                    "Invalid transition: expected " + expected + " but was " + ride.getStatus());
        }
    }

    private RideResponseDTO toDTO(Ride r) {
        return RideResponseDTO.builder()
                .id(r.getId())
                .passengerId(r.getPassengerId())
                .driverId(r.getDriverId())
                .pickupLocation(r.getPickupLocation())
                .destination(r.getDestination())
                .distanceKm(r.getDistanceKm())
                .status(r.getStatus())
                .estimatedFare(r.getEstimatedFare())
                .finalFare(r.getFinalFare())
                .paymentId(r.getPaymentId())
                .requestedAt(r.getRequestedAt())
                .completedAt(r.getCompletedAt())
                .cancelledAt(r.getCancelledAt())
                .cancellationReason(r.getCancellationReason())
                .build();
    }
}