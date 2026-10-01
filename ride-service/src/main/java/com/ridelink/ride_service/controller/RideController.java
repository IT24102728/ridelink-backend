package com.ridelink.ride_service.controller;

import com.ridelink.ride_service.dto.RideRequestDTO;
import com.ridelink.ride_service.dto.RideResponseDTO;
import com.ridelink.ride_service.service.RideService;
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
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride", description = "Ride lifecycle management")
public class RideController {

    private final RideService rideService;

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Request a new ride (creates ride, estimates fare, assigns driver)")
    public ResponseEntity<RideResponseDTO> requestRide(@Valid @RequestBody RideRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.requestRide(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride by id")
    public ResponseEntity<RideResponseDTO> getRide(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRide(id));
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @Operation(summary = "List rides of a passenger")
    public ResponseEntity<List<RideResponseDTO>> getByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(rideService.getRidesByPassenger(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "List rides of a driver")
    public ResponseEntity<List<RideResponseDTO>> getByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(rideService.getRidesByDriver(driverId));
    }

    @PatchMapping("/{rideId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver accepts an assigned ride")
    public ResponseEntity<RideResponseDTO> accept(@PathVariable Long rideId,
                                                  @RequestParam Long driverId) {
        return ResponseEntity.ok(rideService.acceptRide(rideId, driverId));
    }

    @PatchMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver starts the ride")
    public ResponseEntity<RideResponseDTO> start(@PathVariable Long rideId,
                                                 @RequestParam Long driverId) {
        return ResponseEntity.ok(rideService.startRide(rideId, driverId));
    }

    @PatchMapping("/{rideId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver completes the ride (final fare + payment)")
    public ResponseEntity<RideResponseDTO> complete(@PathVariable Long rideId,
                                                    @RequestParam Long driverId) {
        return ResponseEntity.ok(rideService.completeRide(rideId, driverId));
    }

    @PatchMapping("/{rideId}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER')")
    @Operation(summary = "Cancel a ride")
    public ResponseEntity<RideResponseDTO> cancel(@PathVariable Long rideId,
                                                  @RequestParam Long requesterId,
                                                  @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return ResponseEntity.ok(rideService.cancelRide(rideId, requesterId, reason));
    }
}