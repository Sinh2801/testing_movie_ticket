package com.movieticketbooking.controller;

import com.movieticketbooking.request.cinema.CreateCinemaRequest;
import com.movieticketbooking.request.cinema.UpdateCinemaRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.CinemaResponse;
import com.movieticketbooking.service.CinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cinemas")
public class CinemaController {
    private final CinemaService cinemaService;

    @GetMapping("/cinema/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable int id)
    {
        try {
            CinemaResponse cinema = cinemaService.getCinemaById(id);
            return ResponseEntity.ok(new ApiResponse("Get success", cinema));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Can't get cinema", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCinemas()
    {
        try {
            List<CinemaResponse> cinemas = cinemaService.getAllCinemas();
            return ResponseEntity.ok(new ApiResponse("Get all success", cinemas));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Can't get all cinema", e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCinema(
            @RequestBody @Valid CreateCinemaRequest request)
    {
        try {
            CinemaResponse resp = cinemaService.createCinema(request);
            return ResponseEntity.ok(new ApiResponse("Create success", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Can't create cinema", e.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> updateCinema(
            @RequestBody @Valid UpdateCinemaRequest request)
    {
        try {
            CinemaResponse resp = cinemaService.updateCinema(request);
            return ResponseEntity.ok(new ApiResponse("Update success", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Can't update cinema", e.getMessage()));
        }
    }
}
