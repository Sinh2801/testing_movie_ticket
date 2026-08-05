package com.movieticketbooking.repository;

import com.movieticketbooking.model.Seat;

import java.util.List;

public interface SeatRepository extends BaseRepository<Seat, Integer>{
    List<Seat> findByRoomId(int roomId);
}

