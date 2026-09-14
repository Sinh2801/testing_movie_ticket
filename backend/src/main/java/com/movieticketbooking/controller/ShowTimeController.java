package com.movieticketbooking.controller;

import com.movieticketbooking.request.showtime.CreateShowTimeRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.ShowTimeResponse;
import com.movieticketbooking.response.ShowTimeSeatResponse;
import com.movieticketbooking.service.ShowTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ApiResponse> createShowTime(@RequestBody @Valid CreateShowTimeRequest request) 
    {
        ShowTimeResponse resp = showTimeService.createShowTime(request);
        return ResponseEntity.ok(new ApiResponse("Create showtime success", resp));
    }

    // Pagination and filtering of showtimes
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> filter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<ShowTimeResponse> list = showTimeService.filter(page, size);
        return ResponseEntity.ok(new ApiResponse("Filter showtimes success", list));
    }

    // Get showtimes by movie ID
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse> getShowTimesByMovieId(@PathVariable int movieId) 
    {
        List<ShowTimeResponse> list = showTimeService.getShowTimesByMovieId(movieId);
        return ResponseEntity.ok(new ApiResponse("Get showtimes by movie success", list));
    }


    // Get showtime by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getShowTimeById(@PathVariable int id) 
    {
        ShowTimeResponse resp = showTimeService.getShowTimeById(id);
        return ResponseEntity.ok(new ApiResponse("Get showtime detail success", resp));
    }

    // Get seats map by showtime ID
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse> getSeatsByShowTimeId(@PathVariable int id) 
    {
        List<ShowTimeSeatResponse> seats = showTimeService.getSeatsByShowTimeId(id);
        return ResponseEntity.ok(new ApiResponse("Get showtime seats success", seats));
    }
}
