package com.ridelink.driver_service.controller;

import com.ridelink.driver_service.dto.DriverRequestDTO;
import com.ridelink.driver_service.dto.DriverResponseDTO;
import com.ridelink.driver_service.entity.AvailabilityStatus;
import com.ridelink.driver_service.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver", description = "Driver profile, availability and location")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Register a driver profile")
    public ResponseEntity<DriverResponseDTO> create(@Valid @RequestBody DriverRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.createDriver(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver by ID")
    public ResponseEntity<DriverResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get driver by account ID")
    public ResponseEntity<DriverResponseDTO> getByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(driverService.getDriverByAccountId(accountId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all drivers (admin)")
    public ResponseEntity<List<DriverResponseDTO>> getAll() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update driver availability")
    public ResponseEntity<DriverResponseDTO> updateAvailability(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        AvailabilityStatus status = AvailabilityStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(driverService.updateAvailability(id, status));
    }

    @PatchMapping("/{id}/location")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update simulated current location")
    public ResponseEntity<DriverResponseDTO> updateLocation(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(driverService.updateLocation(id, body.get("latitude"), body.get("longitude")));
    }

    @GetMapping("/eligible")
    @Operation(summary = "Get eligible available drivers (used by Ride Service)")
    public ResponseEntity<List<DriverResponseDTO>> getEligible(
            @RequestParam(required = false) String serviceArea) {
        return ResponseEntity.ok(driverService.getEligibleDrivers(serviceArea));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}