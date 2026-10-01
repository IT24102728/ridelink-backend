package com.ridelink.driver_service.service;

import com.ridelink.driver_service.dto.DriverRequestDTO;
import com.ridelink.driver_service.dto.DriverResponseDTO;
import com.ridelink.driver_service.dto.VehicleResponseDTO;
import com.ridelink.driver_service.entity.AvailabilityStatus;
import com.ridelink.driver_service.entity.Driver;
import com.ridelink.driver_service.entity.Vehicle;
import com.ridelink.driver_service.exception.ResourceNotFoundException;
import com.ridelink.driver_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponseDTO createDriver(DriverRequestDTO dto) {
        if (driverRepository.findByAccountId(dto.getAccountId()).isPresent()) {
            throw new IllegalStateException("Driver already exists for accountId: " + dto.getAccountId());
        }
        Driver driver = Driver.builder()
                .accountId(dto.getAccountId())
                .fullName(dto.getFullName())
                .licenseNumber(dto.getLicenseNumber())
                .phoneNumber(dto.getPhoneNumber())
                .serviceArea(dto.getServiceArea())
                .currentLatitude(dto.getCurrentLatitude())
                .currentLongitude(dto.getCurrentLongitude())
                .availability(AvailabilityStatus.OFFLINE)
                .build();
        return toDTO(driverRepository.save(driver));
    }

    public DriverResponseDTO getDriverById(Long id) {
        Driver d = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        return toDTO(d);
    }

    public DriverResponseDTO getDriverByAccountId(Long accountId) {
        Driver d = driverRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found for accountId: " + accountId));
        return toDTO(d);
    }

    public List<DriverResponseDTO> getAllDrivers() {
        return driverRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public DriverResponseDTO updateAvailability(Long driverId, AvailabilityStatus status) {
        Driver d = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
        d.setAvailability(status);
        return toDTO(driverRepository.save(d));
    }

    @Transactional
    public DriverResponseDTO updateLocation(Long driverId, Double lat, Double lon) {
        Driver d = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
        d.setCurrentLatitude(lat);
        d.setCurrentLongitude(lon);
        return toDTO(driverRepository.save(d));
    }

    public List<DriverResponseDTO> getEligibleDrivers(String serviceArea) {
        List<Driver> drivers = (serviceArea == null || serviceArea.isBlank())
                ? driverRepository.findByAvailability(AvailabilityStatus.AVAILABLE)
                : driverRepository.findByAvailabilityAndServiceArea(AvailabilityStatus.AVAILABLE, serviceArea);
        return drivers.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Driver not found: " + id);
        }
        driverRepository.deleteById(id);
    }

    private DriverResponseDTO toDTO(Driver d) {
        VehicleResponseDTO vDto = null;
        if (d.getVehicle() != null) {
            Vehicle v = d.getVehicle();
            vDto = VehicleResponseDTO.builder()
                    .id(v.getId())
                    .driverId(d.getId())
                    .registrationNumber(v.getRegistrationNumber())
                    .make(v.getMake())
                    .model(v.getModel())
                    .year(v.getYear())
                    .color(v.getColor())
                    .passengerCapacity(v.getPassengerCapacity())
                    .build();
        }
        return DriverResponseDTO.builder()
                .id(d.getId())
                .accountId(d.getAccountId())
                .fullName(d.getFullName())
                .licenseNumber(d.getLicenseNumber())
                .phoneNumber(d.getPhoneNumber())
                .availability(d.getAvailability())
                .serviceArea(d.getServiceArea())
                .currentLatitude(d.getCurrentLatitude())
                .currentLongitude(d.getCurrentLongitude())
                .vehicle(vDto)
                .build();
    }
}