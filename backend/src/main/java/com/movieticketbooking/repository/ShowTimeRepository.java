package com.movieticketbooking.repository;

import com.movieticketbooking.model.ShowTime;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface ShowTimeRepository extends BaseRepository<ShowTime, Integer> {
    @EntityGraph(attributePaths = "ticketSeats")
    @Override
    Optional<ShowTime> findById(Integer integer);
}
