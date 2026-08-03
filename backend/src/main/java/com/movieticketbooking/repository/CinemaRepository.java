package com.movieticketbooking.repository;

import com.movieticketbooking.model.Cinema;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface CinemaRepository extends BaseRepository<Cinema, Integer> {
    @EntityGraph(attributePaths = "rooms")
    @Override
    Optional<Cinema> findById(Integer integer);
}
