package com.ridelink.account_service.dto;

import com.ridelink.account_service.entity.User;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 2, max = 80) String fullName,
        @NotNull User.Role role
) {}