package com.ridelink.account_service.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 80) String fullName
) {}