package com.movieticketbooking.controller;

import com.movieticketbooking.model.MovieStatus;
import com.movieticketbooking.request.movie.CreateMovieRequest;
import com.movieticketbooking.request.movie.UpdateMovieRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.MovieResponse;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.service.MovieService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/movies")
public class MovieController {
    private final MovieService movieService;

    // Get movie by id
    @GetMapping("/movie/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Integer id)
    {
        try {
            MovieResponse resp = movieService.getMovieById(id);
            return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Get movie failed", e.getMessage()));
        }
    }

    // Filter movie by name
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> getByName
            (@RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam(required = false) String name)
    {
        try {
            PagedResponse<MovieResponse> resp = movieService.filterName(
                    page, size, name);
            return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Get movie failed", e.getMessage()));
        }
    }

    @GetMapping("/now-showing")
    public ResponseEntity<ApiResponse> getMovieNowShowing
            (@RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size)
    {
        try {
            PagedResponse<MovieResponse> resp = movieService.filterStatus(
                    page, size, MovieStatus.NOW_SHOWING);
            return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Get movie failed", e.getMessage()));
        }
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse> getMovieComingSoon
            (@RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size)
    {
        try {
            PagedResponse<MovieResponse> resp = movieService.filterStatus(
                    page, size, MovieStatus.COMING_SOON);
            return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Get movie failed", e.getMessage()));
        }
    }

    // Add new movie
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addMovie
            (@RequestBody @Valid CreateMovieRequest request)
    {
        try {
            MovieResponse resp = movieService.addMovie(request);
            return ResponseEntity.ok(new ApiResponse("Add movie success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Add movie failed", e.getMessage()));
        }
    }

    // Update movie
    @PostMapping("/edit")
    public ResponseEntity<ApiResponse> updateMovie
            (@RequestBody @Valid UpdateMovieRequest request)
    {
        try {
            MovieResponse resp = movieService.updateMovie(request);
            return ResponseEntity.ok(new ApiResponse("Update movie success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Update movie failed", e.getMessage()));
        }
    }
}
