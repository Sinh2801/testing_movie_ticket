package com.movieticketbooking.request.cinema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Data
public class UpdateCinemaRequest {
    @NotNull
    @Range(min = 1)
    private Integer id;
    @NotBlank
    @Length(min = 1)
    private String name;
    @NotBlank
    @Length(min = 1)
    private String address;
}
