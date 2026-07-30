package com.movieticketbooking.repository;

import com.movieticketbooking.model.Cinema;

import java.util.Optional;

public interface CinemaRepository extends BaseRepository<Cinema, Integer> {
    Optional<Cinema> findByName(String name);
}
