package com.movieticketbooking.controller;

import com.movieticketbooking.request.auth.LoginRequest;
import com.movieticketbooking.request.auth.RefreshTokenRequest;
import com.movieticketbooking.request.auth.RegisterRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.AuthResponse;
import com.movieticketbooking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse resp = authService.login(request);
        return ResponseEntity.ok(new ApiResponse("Login success", resp));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegisterRequest request) {
        AuthResponse resp = authService.register(request);
        return ResponseEntity.ok(new ApiResponse("Register success", resp));
    }

    // Endpoint cấp lại Access Token mới bằng Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        AuthResponse resp = authService.refreshToken(request);
        return ResponseEntity.ok(new ApiResponse("Token refreshed successfully", resp));
    }

    // Endpoint đăng xuất và thu hồi Refresh Token trong DB
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse("Logout successful", null));
    }

    // Endpoint Yêu cầu mã OTP 6 chữ số quên mật khẩu qua Email
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody @Valid com.movieticketbooking.request.auth.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse("Mã OTP 6 chữ số đã được gửi đến email của bạn! Mã có hiệu lực trong 5 phút.", null));
    }

    // Endpoint Xác thực OTP 6 chữ số và Đặt lại Mật khẩu mới
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid com.movieticketbooking.request.auth.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.", null));
    }
}

