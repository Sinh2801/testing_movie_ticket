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
        CinemaResponse cinema = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(new ApiResponse("Get success", cinema));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCinemas()
    {
        List<CinemaResponse> cinemas = cinemaService.getAllCinemas();
        return ResponseEntity.ok(new ApiResponse("Get all success", cinemas));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCinema(
            @RequestBody @Valid CreateCinemaRequest request)
    {
        CinemaResponse resp = cinemaService.createCinema(request);
        return ResponseEntity.ok(new ApiResponse("Create success", resp));
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> updateCinema(
            @RequestBody @Valid UpdateCinemaRequest request)
    {
        CinemaResponse resp = cinemaService.updateCinema(request);
        return ResponseEntity.ok(new ApiResponse("Update success", resp));
    }
}
