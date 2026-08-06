package com.movieticketbooking.repository;

import com.movieticketbooking.model.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    @EntityGraph(attributePaths = {"ticketSeats", "showTime"})
    @Override
    Optional<Ticket> findById(Integer integer);

    List<Ticket> findByUserIdOrderByCreatedAtDesc(int userId);
    Optional<Ticket> findByUid(String uid);
}

