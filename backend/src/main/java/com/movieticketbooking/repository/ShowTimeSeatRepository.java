package com.movieticketbooking.repository;

import com.movieticketbooking.model.ShowTimeSeat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowTimeSeatRepository extends JpaRepository<ShowTimeSeat, Integer> {
    List<ShowTimeSeat> findByShowTimeId(int showTimeId);
}

