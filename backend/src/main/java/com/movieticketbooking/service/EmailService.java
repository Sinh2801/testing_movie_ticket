package com.movieticketbooking.service;

import com.movieticketbooking.model.Ticket;

public interface EmailService {
    void sendTicketConfirmationEmail(Ticket ticket);
    void sendTicketCancellationEmail(Ticket ticket);
}
