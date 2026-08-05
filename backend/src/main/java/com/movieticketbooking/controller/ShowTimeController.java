package com.movieticketbooking.controller;

import com.movieticketbooking.request.showtime.CreateShowTimeRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.ShowTimeResponse;
import com.movieticketbooking.response.ShowTimeSeatResponse;
import com.movieticketbooking.service.ShowTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/showtimes")
@RequiredArgsConstructor
public class ShowTimeController {

    private final ShowTimeService showTimeService;

    // Create showtime (Admin)
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createShowTime(@RequestBody @Valid CreateShowTimeRequest request) {
        try {
            ShowTimeResponse resp = showTimeService.createShowTime(request);
            return ResponseEntity.ok(new ApiResponse("Create showtime success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Create showtime failed: " + e.getMessage(), null));
        }
    }

    // Filter showtimes (Public)
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> filterShowTimes(
            @RequestParam(required = false) Integer cinemaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String movieName) {
        try {
            List<ShowTimeResponse> list = showTimeService.filterShowTimes(cinemaId, date, movieName);
            return ResponseEntity.ok(new ApiResponse("Filter showtimes success", list));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Filter showtimes failed: " + e.getMessage(), null));
        }
    }

    // Get showtimes by movie ID
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse> getShowTimesByMovieId(@PathVariable int movieId) {
        try {
            List<ShowTimeResponse> list = showTimeService.getShowTimesByMovieId(movieId);
            return ResponseEntity.ok(new ApiResponse("Get showtimes by movie success", list));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Get showtimes by movie failed: " + e.getMessage(), null));
        }
    }

    // Get showtime by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getShowTimeById(@PathVariable int id) {
        try {
            ShowTimeResponse resp = showTimeService.getShowTimeById(id);
            return ResponseEntity.ok(new ApiResponse("Get showtime detail success", resp));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Get showtime detail failed: " + e.getMessage(), null));
        }
    }

    // Get seats map by showtime ID
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse> getSeatsByShowTimeId(@PathVariable int id) {
        try {
            List<ShowTimeSeatResponse> seats = showTimeService.getSeatsByShowTimeId(id);
            return ResponseEntity.ok(new ApiResponse("Get showtime seats success", seats));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Get showtime seats failed: " + e.getMessage(), null));
        }
    }
}
