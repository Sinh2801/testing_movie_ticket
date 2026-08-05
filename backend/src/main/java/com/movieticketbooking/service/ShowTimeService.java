package com.movieticketbooking.service;

import com.movieticketbooking.request.showtime.CreateShowTimeRequest;
import com.movieticketbooking.response.ShowTimeResponse;
import com.movieticketbooking.response.ShowTimeSeatResponse;

import java.time.LocalDate;
import java.util.List;

public interface ShowTimeService {
    ShowTimeResponse createShowTime(CreateShowTimeRequest request);
    ShowTimeResponse getShowTimeById(int id);
    List<ShowTimeResponse> filterShowTimes(Integer cinemaId, LocalDate date, String movieName);
    List<ShowTimeResponse> getShowTimesByMovieId(int movieId);
    List<ShowTimeSeatResponse> getSeatsByShowTimeId(int showTimeId);
}
