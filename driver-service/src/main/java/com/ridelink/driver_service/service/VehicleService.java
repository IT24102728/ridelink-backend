package com.ridelink.driver_service.service;

import com.ridelink.driver_service.dto.VehicleRequestDTO;
import com.ridelink.driver_service.dto.VehicleResponseDTO;
import com.ridelink.driver_service.entity.Driver;
import com.ridelink.driver_service.entity.Vehicle;
import com.ridelink.driver_service.exception.ResourceNotFoundException;
import com.ridelink.driver_service.repository.DriverRepository;
import com.ridelink.driver_service.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public VehicleResponseDTO createVehicle(VehicleRequestDTO dto) {
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + dto.getDriverId()));

        vehicleRepository.findByDriverId(driver.getId()).ifPresent(v -> {
            throw new IllegalStateException("Vehicle already exists for driver: " + driver.getId());
        });

        Vehicle v = Vehicle.builder()
                .driver(driver)
                .registrationNumber(dto.getRegistrationNumber())
                .make(dto.getMake())
                .model(dto.getModel())
                .year(dto.getYear())
                .color(dto.getColor())
                .passengerCapacity(dto.getPassengerCapacity())
                .build();
        return toDTO(vehicleRepository.save(v));
    }

    public VehicleResponseDTO getVehicleByDriverId(Long driverId) {
        Vehicle v = vehicleRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for driver: " + driverId));
        return toDTO(v);
    }

    public List<VehicleResponseDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO dto) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
        v.setRegistrationNumber(dto.getRegistrationNumber());
        v.setMake(dto.getMake());
        v.setModel(dto.getModel());
        v.setYear(dto.getYear());
        v.setColor(dto.getColor());
        v.setPassengerCapacity(dto.getPassengerCapacity());
        return toDTO(vehicleRepository.save(v));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    private VehicleResponseDTO toDTO(Vehicle v) {
        return VehicleResponseDTO.builder()
                .id(v.getId())
                .driverId(v.getDriver().getId())
                .registrationNumber(v.getRegistrationNumber())
                .make(v.getMake())
                .model(v.getModel())
                .year(v.getYear())
                .color(v.getColor())
                .passengerCapacity(v.getPassengerCapacity())
                .build();
    }
}