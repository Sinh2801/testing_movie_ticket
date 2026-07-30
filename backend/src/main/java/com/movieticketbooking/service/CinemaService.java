package com.movieticketbooking.service;

import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.request.cinema.CreateCinemaRequest;
import com.movieticketbooking.request.cinema.UpdateCinemaRequest;
import com.movieticketbooking.response.CinemaResponse;

import java.util.List;

public interface CinemaService {
    Cinema findCinemaById(int id);
    CinemaResponse getCinemaById(int id);
    List<CinemaResponse> getAllCinemas();
    CinemaResponse createCinema(CreateCinemaRequest request);
    CinemaResponse updateCinema(UpdateCinemaRequest request);
}
