package com.movieticketbooking.repository;

import com.movieticketbooking.model.Payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByTicketId(int ticketId);
    Optional<Payment> findByTransactionId(String transactionId);
}

