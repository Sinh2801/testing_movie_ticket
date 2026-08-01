package com.movieticketbooking.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Missing email")
    @Email(message = "Invalid email")
    private String email;
    @NotBlank(message = "Missing password")
    private String password;
}
