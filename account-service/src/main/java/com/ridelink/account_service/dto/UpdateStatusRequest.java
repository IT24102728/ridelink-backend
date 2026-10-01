package com.ridelink.account_service.dto;

import com.ridelink.account_service.entity.User;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull User.AccountStatus status) {}