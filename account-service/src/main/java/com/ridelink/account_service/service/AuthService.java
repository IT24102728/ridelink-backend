package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.*;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.exception.ApiException;
import com.ridelink.account_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists");
        }

        User user = new User();
        user.setEmail(req.email().toLowerCase());
        user.setFullName(req.fullName());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        user.setStatus(User.AccountStatus.ACTIVE);

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(),
                UserResponse.from(saved));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Invalid email or password");
        }

        if (user.getStatus() == User.AccountStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "ACCOUNT_SUSPENDED", "Your account is suspended");
        }
        if (user.getStatus() == User.AccountStatus.PENDING) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "ACCOUNT_PENDING", "Your account is not yet activated");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(),
                UserResponse.from(user));
    }
}