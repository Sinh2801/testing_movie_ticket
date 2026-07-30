package com.movieticketbooking.service;

import com.movieticketbooking.model.Genre;
import com.movieticketbooking.request.gerne.CreateGenreRequest;
import com.movieticketbooking.request.gerne.UpdateGenreRequest;
import com.movieticketbooking.response.GenreResponse;

import java.util.List;

public interface GenreService {
    List<GenreResponse> getAllGenres();
    Genre findGenreById(short id);
    GenreResponse getById(short id);
    GenreResponse addGenre(CreateGenreRequest request);
    GenreResponse updateGenre(UpdateGenreRequest request);
}
