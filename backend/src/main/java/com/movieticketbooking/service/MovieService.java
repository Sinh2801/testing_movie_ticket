package com.movieticketbooking.service;

import com.movieticketbooking.model.Genre;
import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.MovieStatus;
import com.movieticketbooking.request.movie.CreateMovieRequest;
import com.movieticketbooking.request.movie.UpdateMovieRequest;
import com.movieticketbooking.response.MovieResponse;
import com.movieticketbooking.response.PagedResponse;

public interface MovieService {
    PagedResponse<MovieResponse> filter(int page, int size,
                                        String name, short genreId,
                                        MovieStatus movieStatus, String country);
    Movie findMovieById(int id);
    MovieResponse getMovieById(int id);
    MovieResponse addMovie(CreateMovieRequest request);
    MovieResponse updateMovie(UpdateMovieRequest request);
}
