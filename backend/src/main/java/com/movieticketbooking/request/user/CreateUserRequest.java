package com.movieticketbooking.request.user;

import com.movieticketbooking.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    @Size(min = 1, max = 20)
    private String username;
    @NotBlank
    @Pattern(
            regexp = "^[a-z0-9]+$",
            message = "Password must only contain lowercase letters and numbers"
    )
    private String password;
    @NotBlank
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")
    private String email;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private Role role;

    // for register
    public CreateUserRequest(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = Role.USER;
    }
}
