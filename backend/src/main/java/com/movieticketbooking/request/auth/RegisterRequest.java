package com.movieticketbooking.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterRequest {
    @NotBlank(message = "Missing name")
    @Length(min=3, max=20, message = "Missing name")
    private String name;
    @NotBlank(message = "Missing email")
    @Email(message = "Invalid email")
    private String email;
    @NotBlank(message = "Missing phone number")
    private String phoneNumber;
    @NotBlank(message = "Missing password")
    private String password;
    @NotBlank(message = "Missing confirm password")
    private String confirmPassword;
}
