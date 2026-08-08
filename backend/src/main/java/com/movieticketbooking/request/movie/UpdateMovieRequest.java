package com.movieticketbooking.request.movie;

import com.movieticketbooking.model.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateMovieRequest {
    private int id;
    @NotBlank
    private String name;
    @NotBlank
    private String directorName;
    @NotBlank
    private String actorsName;
    private String description;
    private int duration;
    @NotBlank
    private String country;
    @NotBlank
    private String imageUrl;
    private MovieStatus movieStatus;
    private LocalDateTime showAt;
}
