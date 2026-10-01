package com.ridelink.account_service.controller;

import com.ridelink.account_service.dto.*;
import com.ridelink.account_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Profile & account status management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        Long id = Long.valueOf(auth.getName());
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated user's profile")
    public ResponseEntity<UserResponse> updateMe(Authentication auth,
                                                 @Valid @RequestBody UpdateProfileRequest req) {
        Long id = Long.valueOf(auth.getName());
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")
    @Operation(summary = "Get a user by id (admin or self)")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status (admin only)")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(userService.updateStatus(id, req.status()));
    }
}