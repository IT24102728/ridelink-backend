package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.*;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.exception.ApiException;
import com.ridelink.account_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setup() {
        existingUser = new User();
        existingUser.setEmail("pass@ridelink.lk");
        existingUser.setFullName("Pass One");
        existingUser.setPasswordHash("encoded");
        existingUser.setRole(User.Role.PASSENGER);
        existingUser.setStatus(User.AccountStatus.ACTIVE);
    }

    @Test
    void register_shouldCreateUser_whenEmailIsNew() {
        RegisterRequest req = new RegisterRequest(
                "new@ridelink.lk", "Pass@1234", "New User", User.Role.PASSENGER);

        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse resp = authService.register(req);

        assertThat(resp.token()).isEqualTo("jwt-token");
        assertThat(resp.user().email()).isEqualTo("new@ridelink.lk");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest req = new RegisterRequest(
                "pass@ridelink.lk", "Pass@1234", "Dup", User.Role.PASSENGER);
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        LoginRequest req = new LoginRequest("pass@ridelink.lk", "Pass@1234");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(req.password(), "encoded")).thenReturn(true);
        when(jwtService.generateToken(existingUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse resp = authService.login(req);

        assertThat(resp.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_shouldThrow_whenPasswordWrong() {
        LoginRequest req = new LoginRequest("pass@ridelink.lk", "wrong");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(req.password(), "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldThrow_whenUserMissing() {
        LoginRequest req = new LoginRequest("ghost@ridelink.lk", "Pass@1234");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ApiException.class);
    }
}