package com.movieticketbooking.repository;

import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends BaseRepository<Movie, Integer> {
    @Query("""
        SELECT DISTINCT m
        FROM Movie m
        LEFT JOIN m.genres g
        WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:genreId IS NULL OR g.id = :genreId)
          AND (:movieStatus IS NULL OR m.movieStatus = :movieStatus)
          AND (:country IS NULL OR LOWER(m.country) LIKE LOWER(CONCAT('%', :country, '%')))
        """)
    Page<Movie> filter(
            @Param("name") String name,
            @Param("genreId") Short genreId,
            @Param("movieStatus") MovieStatus movieStatus,
            @Param("country") String country,
            Pageable pageable
    );
}
