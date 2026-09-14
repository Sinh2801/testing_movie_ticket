package com.movieticketbooking.response;

import com.movieticketbooking.model.MovieStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MovieResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String directorName;
    private String actorsName;
    private String description;
    private int duration;
    private String country;
    private String imageUrl;
    private double rating;
    private List<String> genres;
    private MovieStatus movieStatus;
    private LocalDateTime showAt;
    private LocalDateTime createdAt;
}
