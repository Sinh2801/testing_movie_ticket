package com.movieticketbooking.repository;

import com.movieticketbooking.model.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends BaseRepository<Ticket, Integer> {
    @EntityGraph(attributePaths = {"ticketSeats", "showTime"})
    @Override
    Optional<Ticket> findById(Integer integer);

    List<Ticket> findByUserIdOrderByCreatedAtDesc(int userId);
    Optional<Ticket> findByUid(String uid);
}

