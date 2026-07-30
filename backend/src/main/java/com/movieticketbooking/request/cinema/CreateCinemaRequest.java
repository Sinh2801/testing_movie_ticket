package com.movieticketbooking.request.cinema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateCinemaRequest {
    @Length(min = 1, max = 50)
    @NotBlank
    private String name;
    @Length(min = 1, max = 100)
    @NotBlank
    private String address;
}
