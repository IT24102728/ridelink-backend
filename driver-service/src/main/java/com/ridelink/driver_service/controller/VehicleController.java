package com.ridelink.driver_service.controller;

import com.ridelink.driver_service.dto.VehicleRequestDTO;
import com.ridelink.driver_service.dto.VehicleResponseDTO;
import com.ridelink.driver_service.service.VehicleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Vehicle registration and details")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    public ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody VehicleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(dto));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<VehicleResponseDTO> getByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(vehicleService.getVehicleByDriverId(driverId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VehicleResponseDTO>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    public ResponseEntity<VehicleResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody VehicleRequestDTO dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}