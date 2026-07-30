package com.movieticketbooking.controller;

import com.movieticketbooking.model.Genre;
import com.movieticketbooking.request.gerne.CreateGenreRequest;
import com.movieticketbooking.request.gerne.UpdateGenreRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.GenreResponse;
import com.movieticketbooking.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/genres")
public class GenreController {
    private final GenreService genreService;

    // Get all
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAll()
    {
        try {
            List<GenreResponse> genres = genreService.getAllGenres();
            return ResponseEntity.ok(new ApiResponse("Get all genres success", genres));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Get all genres failed", e.getMessage()));
        }
    }

    // Get by id
    @GetMapping("/genre/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable short id)
    {
        try {
            GenreResponse genre = genreService.getById(id);
            return ResponseEntity.ok(new ApiResponse("Get genre success", genre));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Get genre failed", e.getMessage()));
        }
    }

    // Add new genre
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addGenre(@RequestBody @Valid CreateGenreRequest request)
    {
        try {
            GenreResponse genre = genreService.addGenre(request);
            return ResponseEntity.ok(new ApiResponse("Create genre success", genre));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Create genre failed", e.getMessage()));
        }
    }

    // Update genre
    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> updateGenre(@RequestBody @Valid UpdateGenreRequest request)
    {
        try {
            GenreResponse genre = genreService.updateGenre(request);
            return ResponseEntity.ok(new ApiResponse("Update genre success", genre));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Update genre failed", e.getMessage()));
        }
    }
}
