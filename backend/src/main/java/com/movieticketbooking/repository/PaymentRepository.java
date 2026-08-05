package com.movieticketbooking.repository;

import com.movieticketbooking.model.Payment;

import java.util.Optional;

public interface PaymentRepository extends BaseRepository<Payment, Integer> {
    Optional<Payment> findByTicketId(int ticketId);
    Optional<Payment> findByTransactionId(String transactionId);
}

