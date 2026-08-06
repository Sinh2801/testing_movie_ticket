package com.movieticketbooking.repository;

import com.movieticketbooking.model.Cinema;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CinemaRepository extends JpaRepository<Cinema, Integer> {
    @EntityGraph(attributePaths = "rooms")
    @Override
    Optional<Cinema> findById(Integer integer);
}
