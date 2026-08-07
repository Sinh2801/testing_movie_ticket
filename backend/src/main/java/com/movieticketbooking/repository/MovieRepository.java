package com.movieticketbooking.repository;

import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    @Query("""
        SELECT DISTINCT m
        FROM Movie m
        WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:movieStatus IS NULL OR m.movieStatus = :movieStatus)
        """)
    Page<Movie> filter(
            @Param("name") String name,
            @Param("movieStatus") MovieStatus movieStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "genres")
    @Override
    Optional<Movie> findById(Integer integer);
}
