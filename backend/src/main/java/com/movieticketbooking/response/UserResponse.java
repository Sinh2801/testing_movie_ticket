package com.movieticketbooking.response;

import com.movieticketbooking.model.Role;
import lombok.Data;

@Data
public class UserResponse {
    private int id;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
}
