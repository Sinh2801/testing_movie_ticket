package com.movieticketbooking.repository;

import com.movieticketbooking.model.ShowTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ShowTimeRepository extends JpaRepository<ShowTime, Integer> {                            
    @Query("SELECT s FROM ShowTime s WHERE s.movie.id = :movieId AND " +
           "(s.startTime > NOW()) " +
           "ORDER BY s.startTime ASC")
    List<ShowTime> findByMovieId(@Param("movieId") int movieId);

}

