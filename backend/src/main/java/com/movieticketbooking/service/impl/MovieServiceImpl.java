package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Genre;
import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.MovieStatus;
import com.movieticketbooking.repository.GenreRepository;
import com.movieticketbooking.repository.MovieRepository;
import com.movieticketbooking.request.movie.CreateMovieRequest;
import com.movieticketbooking.request.movie.UpdateMovieRequest;
import com.movieticketbooking.response.GenreResponse;
import com.movieticketbooking.response.MovieResponse;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.service.GenreService;
import com.movieticketbooking.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final GenreService genreService;

    // Filter movie by name, genre, movie status, country
    @Override
    public PagedResponse<MovieResponse> filter(int page,
            int size,
            String name,
            short genreId,
            MovieStatus movieStatus,
            String country) {
        Genre genre = genreService.findGenreById(genreId);
        if (genre == null)
            throw new ResourceNotFoundException("Genre not found");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<Movie> moviePage = movieRepository.filter(name, genreId, movieStatus, country, pageable);
        Page<MovieResponse> movieResponsePage = moviePage.map(this::toResponse);
        return new PagedResponse<>(movieResponsePage);
    }

    // Find movie by id
    @Override
    public Movie findMovieById(int id) {
        Optional<Movie> movie = movieRepository.findById(id);
        if (movie.isEmpty())
            throw new ResourceNotFoundException("Movie not found");
        return movie.get();
    }

    // Get movie by id
    @Override
    public MovieResponse getMovieById(int id) {
        return toResponse(findMovieById(id));
    }

    // Add new movie
    @Override
    public MovieResponse addMovie(CreateMovieRequest request) {
        Movie movie = new Movie();
        movie.setName(request.getName());
        movie.setDirectorName(request.getDirectorName());
        movie.setActorsName(request.getActorsName());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setCountry(request.getCountry());
        movie.setImageUrl(request.getImageUrl());
        List<Genre> genres = request.getGenres()
                .stream()
                .map(genreService::findGenreById)
                .toList();
        movie.setGenres(genres);
        movie.setShowAt(request.getShowAt());
        movie.setCreatedAt(LocalDateTime.now());

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // update movie
    @Override
    public MovieResponse updateMovie(UpdateMovieRequest request) {
        Optional<Movie> opt = movieRepository.findById(request.getId());
        if (opt.isEmpty())
            throw new ResourceNotFoundException("Movie not found");

        Movie movie = opt.get();
        movie.setName(request.getName());
        movie.setDirectorName(request.getDirectorName());
        movie.setActorsName(request.getActorsName());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setCountry(request.getCountry());
        movie.setImageUrl(request.getImageUrl());
        movie.setShowAt(request.getShowAt());

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // mapper
    private MovieResponse toResponse(Movie movie) {
        MovieResponse movieResponse = new MovieResponse();
        movieResponse.setId(movie.getId());
        movieResponse.setName(movie.getName());
        movieResponse.setDirectorName(movie.getDirectorName());
        movieResponse.setActorsName(movie.getActorsName());
        movieResponse.setDescription(movie.getDescription());
        movieResponse.setDuration(movie.getDuration());
        movieResponse.setCountry(movie.getCountry());
        movieResponse.setImageUrl(movie.getImageUrl());
        movieResponse.setRating(movie.getRating());
        List<String> genres = movie.getGenres().stream()
                .map(Genre::getName).toList();
        movieResponse.setGenres(genres);
        movieResponse.setShowAt(movie.getShowAt());
        movieResponse.setCreatedAt(movie.getCreatedAt());
        return movieResponse;
    }
}
