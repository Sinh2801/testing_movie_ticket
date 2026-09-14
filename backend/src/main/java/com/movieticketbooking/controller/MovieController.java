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
        MovieResponse resp = movieService.getMovieById(id);
        return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
    }

    // Filter movie by name
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> getByName
            (@RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam(required = false) String name)
    {
        PagedResponse<MovieResponse> resp = movieService.filterName(
                    page, size, name);
        return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
    }

    @GetMapping("/now-showing")
    public ResponseEntity<ApiResponse> getMovieNowShowing
            (@RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size)
    {
        PagedResponse<MovieResponse> resp = movieService.filterStatus(
                    page, size, MovieStatus.NOW_SHOWING);
        return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse> getMovieComingSoon
            (@RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size)
    {
        PagedResponse<MovieResponse> resp = movieService.filterStatus(
                    page, size, MovieStatus.COMING_SOON);
        return ResponseEntity.ok(new ApiResponse("Get movie success", resp));
    }

    // Add new movie
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addMovie
            (@RequestBody @Valid CreateMovieRequest request)
    {
        MovieResponse resp = movieService.addMovie(request);
        return ResponseEntity.ok(new ApiResponse("Add movie success", resp));
    }

    // Update movie
    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> updateMovie
            (@RequestBody @Valid UpdateMovieRequest request)
    {
        MovieResponse resp = movieService.updateMovie(request);
        return ResponseEntity.ok(new ApiResponse("Update movie success", resp));
    }
}
