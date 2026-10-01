package com.ridelink.account_service.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {}