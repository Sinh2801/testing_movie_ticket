package com.movieticketbooking.controller;

import com.movieticketbooking.request.auth.LoginRequest;
import com.movieticketbooking.request.auth.RegisterRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.AuthResponse;
import com.movieticketbooking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @RequestBody @Valid LoginRequest request)
    {
        try {
            AuthResponse resp = authService.login(request);
            return ResponseEntity.ok(new ApiResponse("Login success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Login failed", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @RequestBody @Valid RegisterRequest request)
    {
        try {
            AuthResponse resp = authService.register(request);
            return ResponseEntity.ok(new ApiResponse("Register success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Register failed", e.getMessage()));
        }
    }
}
