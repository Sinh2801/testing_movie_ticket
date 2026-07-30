package com.movieticketbooking.response;

import com.movieticketbooking.model.MovieStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MovieResponse {
    private int id;
    private String name;
    private String directorName;
    private String actorsName;
    private String description;
    private int duration;
    private String country;
    private String imageUrl;
    private List<String> genres;
    private MovieStatus movieStatus;
    private LocalDateTime startAt;
    private LocalDateTime createdAt;
}
