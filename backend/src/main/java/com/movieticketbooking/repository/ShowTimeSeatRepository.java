package com.movieticketbooking.repository;

import com.movieticketbooking.model.ShowTimeSeat;

import java.util.List;

public interface ShowTimeSeatRepository extends BaseRepository<ShowTimeSeat, Integer> {
    List<ShowTimeSeat> findByShowTimeId(int showTimeId);
}

