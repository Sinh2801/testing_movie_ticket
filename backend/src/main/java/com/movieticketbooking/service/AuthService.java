package com.movieticketbooking.service;

import com.movieticketbooking.request.auth.LoginRequest;
import com.movieticketbooking.request.auth.RefreshTokenRequest;
import com.movieticketbooking.request.auth.RegisterRequest;
import com.movieticketbooking.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    void forgotPassword(com.movieticketbooking.request.auth.ForgotPasswordRequest request);
    void resetPassword(com.movieticketbooking.request.auth.ResetPasswordRequest request);
}
