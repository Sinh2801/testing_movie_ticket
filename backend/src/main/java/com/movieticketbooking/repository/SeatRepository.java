package com.movieticketbooking.repository;

import com.movieticketbooking.model.Seat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Integer>{
    List<Seat> findByRoomId(int roomId);
}

