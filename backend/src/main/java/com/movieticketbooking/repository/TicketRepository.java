package com.movieticketbooking.repository;

import com.movieticketbooking.model.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    // Load đầy đủ quan hệ cần thiết trong 1 query → tránh N+1 Problem
    @EntityGraph(attributePaths = {
        "ticketSeats",
        "ticketSeats.showTimeSeat",
        "ticketSeats.showTimeSeat.seat",
        "showTime",
        "showTime.movie",
        "showTime.cinema",
        "showTime.room",
        "user"
    })
    @Override
    Optional<Ticket> findById(Integer integer);

    @EntityGraph(attributePaths = {
        "ticketSeats",
        "ticketSeats.showTimeSeat",
        "ticketSeats.showTimeSeat.seat",
        "showTime",
        "showTime.movie",
        "showTime.cinema",
        "showTime.room",
        "user"
    })
    @Override
    Page<Ticket> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
        "ticketSeats",
        "ticketSeats.showTimeSeat",
        "ticketSeats.showTimeSeat.seat",
        "showTime",
        "showTime.movie",
        "showTime.cinema",
        "showTime.room",
        "user"
    })
    Page<Ticket> findByUidContaining(String uid, Pageable pageable);

    @EntityGraph(attributePaths = {
        "ticketSeats",
        "ticketSeats.showTimeSeat",
        "ticketSeats.showTimeSeat.seat",
        "showTime",
        "showTime.movie",
        "showTime.cinema",
        "showTime.room"
    })
    List<Ticket> findByUserIdOrderByCreatedAtDesc(int userId);

    Optional<Ticket> findByUid(String uid);

    @EntityGraph(attributePaths = {
        "ticketSeats",
        "ticketSeats.showTimeSeat"
    })
    List<Ticket> findByStatusAndCreatedAtBefore(com.movieticketbooking.model.TicketStatus status, java.time.LocalDateTime dateTime);
}

