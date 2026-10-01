package com.ridelink.ride_service.service;

import com.ridelink.ride_service.client.DriverServiceClient;
import com.ridelink.ride_service.client.FareServiceClient;
import com.ridelink.ride_service.dto.*;
import com.ridelink.ride_service.entity.Ride;
import com.ridelink.ride_service.entity.RideStatus;
import com.ridelink.ride_service.exception.InvalidStateTransitionException;
import com.ridelink.ride_service.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private DriverServiceClient driverServiceClient;
    @Mock private FareServiceClient fareServiceClient;

    @InjectMocks private RideService rideService;

    @Test
    void requestRide_success() {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPassengerId(1L);
        dto.setPickupLocation("A");
        dto.setDestination("B");
        dto.setDistanceKm(5.0);

        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> {
            Ride r = i.getArgument(0);
            if (r.getId() == null) r.setId(100L);
            return r;
        });

        FareEstimateResponseDTO fare = new FareEstimateResponseDTO();
        fare.setEstimatedFare(750.0);
        when(fareServiceClient.estimateFare(any())).thenReturn(fare);

        DriverDTO d = new DriverDTO();
        d.setId(50L);
        when(driverServiceClient.getEligibleDrivers(any())).thenReturn(List.of(d));

        RideResponseDTO result = rideService.requestRide(dto);
        assertEquals(RideStatus.ASSIGNED, result.getStatus());
        assertEquals(50L, result.getDriverId());
        assertEquals(750.0, result.getEstimatedFare());
    }

    @Test
    void requestRide_noDriver_throws() {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPassengerId(1L);
        dto.setPickupLocation("A");
        dto.setDestination("B");

        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> {
            Ride r = i.getArgument(0);
            r.setId(101L);
            return r;
        });
        FareEstimateResponseDTO fare = new FareEstimateResponseDTO();
        fare.setEstimatedFare(500.0);
        when(fareServiceClient.estimateFare(any())).thenReturn(fare);
        when(driverServiceClient.getEligibleDrivers(any())).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> rideService.requestRide(dto));
    }

    @Test
    void acceptRide_wrongDriver_throws() {
        Ride ride = Ride.builder().id(1L).driverId(10L)
                .status(RideStatus.ASSIGNED).build();
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStateTransitionException.class,
                () -> rideService.acceptRide(1L, 99L));
    }

    @Test
    void startRide_invalidState_throws() {
        Ride ride = Ride.builder().id(1L).driverId(10L)
                .status(RideStatus.REQUESTED).build();
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStateTransitionException.class,
                () -> rideService.startRide(1L, 10L));
    }
}