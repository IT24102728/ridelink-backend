package com.ridelink.driver_service.service;

import com.ridelink.driver_service.dto.DriverRequestDTO;
import com.ridelink.driver_service.dto.DriverResponseDTO;
import com.ridelink.driver_service.entity.AvailabilityStatus;
import com.ridelink.driver_service.entity.Driver;
import com.ridelink.driver_service.exception.ResourceNotFoundException;
import com.ridelink.driver_service.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock private DriverRepository driverRepository;
    @InjectMocks private DriverService driverService;

    @Test
    void createDriver_success() {
        DriverRequestDTO dto = new DriverRequestDTO();
        dto.setAccountId(100L);
        dto.setFullName("John Doe");
        dto.setLicenseNumber("B1234567");

        when(driverRepository.findByAccountId(100L)).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenAnswer(i -> {
            Driver d = i.getArgument(0);
            d.setId(1L);
            return d;
        });

        DriverResponseDTO result = driverService.createDriver(dto);
        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        assertEquals(AvailabilityStatus.OFFLINE, result.getAvailability());
    }

    @Test
    void createDriver_duplicate_throws() {
        when(driverRepository.findByAccountId(100L))
                .thenReturn(Optional.of(new Driver()));

        DriverRequestDTO dto = new DriverRequestDTO();
        dto.setAccountId(100L);
        assertThrows(IllegalStateException.class, () -> driverService.createDriver(dto));
    }

    @Test
    void getDriver_notFound_throws() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverById(99L));
    }
}