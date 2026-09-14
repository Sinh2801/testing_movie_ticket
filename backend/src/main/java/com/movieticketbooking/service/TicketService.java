package com.movieticketbooking.service;

import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.User;
import com.movieticketbooking.request.ticket.CreateTicketRequest;
import com.movieticketbooking.response.PagedResponse;
import com.movieticketbooking.response.TicketResponse;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(User user, CreateTicketRequest request);
    TicketResponse getTicketById(int id, User currentUser);
    Ticket findTicketById(int id);
    TicketResponse getTicketByUid(String uid, User currentUser);
    List<TicketResponse> getUserTickets(User user);
    void confirmTicketPayment(int ticketId);
    TicketResponse cancelTicket(int ticketId, User user);

    PagedResponse<TicketResponse> filter(int page, int size, String uid);
}

