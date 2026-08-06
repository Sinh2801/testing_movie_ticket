package com.movieticketbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieticketbooking.model.TicketSeat;

public interface TicketSeatRepository extends JpaRepository<TicketSeat, Integer> {
}
