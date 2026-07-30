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

    // Filter movie
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> filter
            (@RequestParam int size,
             @RequestParam int page,
             @RequestParam String name,
             @RequestParam short genreId,
             @RequestParam MovieStatus movieStatus,
             @RequestParam String country)
    {
        try {
            PagedResponse<MovieResponse> resp = movieService.filter(
                    size, page, name, genreId, movieStatus, country);
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
