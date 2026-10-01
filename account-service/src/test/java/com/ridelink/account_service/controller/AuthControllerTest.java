package com.ridelink.account_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.account_service.config.SecurityConfig;
import com.ridelink.account_service.dto.*;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.exception.ApiException;
import com.ridelink.account_service.exception.GlobalExceptionHandler;
import com.ridelink.account_service.security.JwtAuthFilter;
import com.ridelink.account_service.service.AuthService;
import com.ridelink.account_service.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthFilter.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtService jwtService;   // needed by JwtAuthFilter

    @Test
    void register_shouldReturn201_andToken() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "new@ridelink.lk", "Pass@1234", "New User", User.Role.PASSENGER);

        AuthResponse resp = new AuthResponse("jwt", "Bearer", 3600000L,
                new UserResponse(1L, "new@ridelink.lk", "New User",
                        User.Role.PASSENGER, User.AccountStatus.ACTIVE, null));

        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.user.email").value("new@ridelink.lk"));
    }

    @Test
    void register_shouldReturn400_whenEmailInvalid() throws Exception {
        String body = """
                {"email":"not-an-email","password":"Pass@1234","fullName":"X","role":"PASSENGER"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn401_whenServiceThrows() throws Exception {
        LoginRequest req = new LoginRequest("pass@ridelink.lk", "wrong");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ApiException(HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }
}