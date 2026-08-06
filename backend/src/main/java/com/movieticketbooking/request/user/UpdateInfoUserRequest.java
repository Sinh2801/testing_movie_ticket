package com.movieticketbooking.request.user;

import com.movieticketbooking.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateInfoUserRequest {
    private int id;
    @NotBlank
    @Size(min = 1, max = 20)
    private String name;

    @NotBlank
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")
    private String email;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private Role role;
}
