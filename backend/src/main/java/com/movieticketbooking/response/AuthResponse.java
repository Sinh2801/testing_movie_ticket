package com.movieticketbooking.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String name;
    private String email;
    private String role;
}
