package com.movieticketbooking.service;

import com.movieticketbooking.request.auth.LoginRequest;
import com.movieticketbooking.request.auth.RegisterRequest;
import com.movieticketbooking.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
