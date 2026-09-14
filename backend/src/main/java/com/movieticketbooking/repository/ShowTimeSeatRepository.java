package com.movieticketbooking.repository;

import com.movieticketbooking.model.ShowTimeSeat;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowTimeSeatRepository extends JpaRepository<ShowTimeSeat, Integer> {
    List<ShowTimeSeat> findByShowTimeId(int showTimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowTimeSeat s WHERE s.id = :id")
    Optional<ShowTimeSeat> findByIdWithLock(@Param("id") Integer id);
}
