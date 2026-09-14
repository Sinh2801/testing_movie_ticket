package com.movieticketbooking.request.movie;

import com.movieticketbooking.model.MovieStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateMovieRequest {
    @NotBlank(message = "Movie name is required")
    private String name;

    @NotBlank(message = "Director name is required")
    private String directorName;

    @NotBlank(message = "Actors name is required")
    private String actorsName;

    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration must not exceed 600 minutes")
    private int duration;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must not exceed 10.0")
    private double rating;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @Size(min = 1, message = "At least 1 genre is required")
    private List<Short> genres;

    private MovieStatus movieStatus;
    private LocalDateTime showAt;
}
