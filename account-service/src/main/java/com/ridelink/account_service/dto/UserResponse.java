package com.ridelink.account_service.dto;

import com.ridelink.account_service.entity.User;
import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        User.Role role,
        User.AccountStatus status,
        Instant createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(),
                u.getRole(), u.getStatus(), u.getCreatedAt());
    }
}