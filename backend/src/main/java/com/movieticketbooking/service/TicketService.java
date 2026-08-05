package com.movieticketbooking.service;

import com.movieticketbooking.model.User;
import com.movieticketbooking.request.ticket.CreateTicketRequest;
import com.movieticketbooking.response.TicketResponse;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(User user, CreateTicketRequest request);
    TicketResponse getTicketById(int id);
    TicketResponse getTicketByUid(String uid);
    List<TicketResponse> getUserTickets(User user);
    void confirmTicketPayment(int ticketId);
    TicketResponse cancelTicket(int ticketId, User user);
}
