package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Genre;
import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.MovieStatus;
import com.movieticketbooking.repository.MovieRepository;
import com.movieticketbooking.request.movie.CreateMovieRequest;
import com.movieticketbooking.request.movie.UpdateMovieRequest;
import com.movieticketbooking.response.MovieResponse;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.service.GenreService;
import com.movieticketbooking.service.MovieService;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final GenreService genreService;

    // Filter movie by name
    @Override
    public PagedResponse<MovieResponse> filterName(int page,
            int size,
            String name) 
    {
        int pageIndex = (page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Movie> moviePage = null;
        if (name == null || name.isBlank()) {
            moviePage = movieRepository.findAll(pageable);
        } else {
            moviePage = movieRepository.filter(name, null, pageable);
        }
        Page<MovieResponse> movieResponsePage = moviePage.map(this::toResponse);
        return new PagedResponse<>(movieResponsePage);
    }

    // Filter movie by status
    @Override
    public PagedResponse<MovieResponse> filterStatus(int page,
            int size,
            MovieStatus movieStatus) 
    {
        int pageIndex = (page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Movie> moviePage = movieRepository.filter(null, movieStatus, pageable);
        Page<MovieResponse> movieResponsePage = moviePage.map(this::toResponse);
        return new PagedResponse<>(movieResponsePage);
    }

    // Find movie by id
    @Override
    public Movie findMovieById(int id) 
    {
        Optional<Movie> movie = movieRepository.findById(id);
        if (movie.isEmpty())
            throw new ResourceNotFoundException("Movie not found");
        return movie.get();
    }

    // Get movie by id (Cache dữ liệu MovieResponse vào Redis)
    @Cacheable(value = "movies", key = "'movie:' + #id")
    @Override
    public MovieResponse getMovieById(int id) 
    {
        return toResponse(findMovieById(id));
    }

    // Add new movie
    @CacheEvict(value = "movies", allEntries = true)
    @Override
    public MovieResponse addMovie(CreateMovieRequest request) 
    {
        Movie movie = new Movie();
        movie.setName(request.getName());
        movie.setDirectorName(request.getDirectorName());
        movie.setActorsName(request.getActorsName());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setCountry(request.getCountry());
        movie.setImageUrl(request.getImageUrl());
        movie.setMovieStatus(request.getMovieStatus() != null ? request.getMovieStatus() : MovieStatus.NOW_SHOWING);
        if (request.getGenres() != null) {
            List<Genre> genres = request.getGenres()
                    .stream()
                    .map(genreService::findGenreById)
                    .toList();
            movie.setGenres(genres);
        }
        movie.setShowAt(request.getShowAt());
        movie.setCreatedAt(LocalDateTime.now());

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // update movie
    @CacheEvict(value = "movies", key = "'movie:' + #request.id")
    @Override
    public MovieResponse updateMovie(UpdateMovieRequest request) 
    {
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
        if (request.getMovieStatus() != null) {
            movie.setMovieStatus(request.getMovieStatus());
        }
        movie.setShowAt(request.getShowAt());

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // mapper
    private MovieResponse toResponse(Movie movie) 
    {
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
        movieResponse.setMovieStatus(movie.getMovieStatus());
        List<String> genres = movie.getGenres().stream()
                .map(Genre::getName).toList();
        movieResponse.setGenres(genres);
        movieResponse.setShowAt(movie.getShowAt());
        movieResponse.setCreatedAt(movie.getCreatedAt());
        return movieResponse;
    }
}
