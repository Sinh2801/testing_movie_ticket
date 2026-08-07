package com.movieticketbooking.request.movie;

import com.movieticketbooking.model.Genre;
import com.movieticketbooking.model.MovieStatus;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateMovieRequest {
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
    @Size(min = 1)
    private List<Short> genres;
    private MovieStatus movieStatus;
    private LocalDateTime showAt;
}
