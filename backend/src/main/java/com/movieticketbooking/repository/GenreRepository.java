package com.movieticketbooking.repository;

import com.movieticketbooking.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Short> {
}
