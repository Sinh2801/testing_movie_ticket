package com.movieticketbooking.repository;

import com.movieticketbooking.model.ShowTime;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowTimeRepository extends BaseRepository<ShowTime, Integer> {
    
    @Query("SELECT s FROM ShowTime s WHERE " +
           "(:cinemaId IS NULL OR s.cinema.id = :cinemaId) AND " +
           "(:startTime IS NULL OR s.startTime >= :startTime) AND " +
           "(:endTime IS NULL OR s.startTime <= :endTime) AND " +
           "(:movieName IS NULL OR :movieName = '' OR LOWER(s.movie.name) LIKE LOWER(CONCAT('%', :movieName, '%'))) " +
           "ORDER BY s.startTime ASC")
    List<ShowTime> filterShowTimes(@Param("cinemaId") Integer cinemaId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("movieName") String movieName);

    List<ShowTime> findByMovieId(int movieId);
}

