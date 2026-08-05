package com.movieticketbooking.controller;

import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.ticket.CreateTicketRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.TicketResponse;
import com.movieticketbooking.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    // Create new ticket booking
    @PostMapping("/book")
    public ResponseEntity<ApiResponse> createTicket(@RequestBody @Valid CreateTicketRequest request) {
        try {
            User user = getAuthenticatedUser();
            TicketResponse response = ticketService.createTicket(user, request);
            return ResponseEntity.ok(new ApiResponse("Booking ticket success", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Booking ticket failed: " + e.getMessage(), null));
        }
    }

    // Get ticket history for logged in user
    @GetMapping("/history")
    public ResponseEntity<ApiResponse> getUserTicketHistory() {
        try {
            User user = getAuthenticatedUser();
            List<TicketResponse> list = ticketService.getUserTickets(user);
            return ResponseEntity.ok(new ApiResponse("Get user ticket history success", list));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Get ticket history failed: " + e.getMessage(), null));
        }
    }

    // Get ticket details by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTicketById(@PathVariable int id) {
        try {
            TicketResponse response = ticketService.getTicketById(id);
            return ResponseEntity.ok(new ApiResponse("Get ticket detail success", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Get ticket detail failed: " + e.getMessage(), null));
        }
    }

    // Get ticket details by UID
    @GetMapping("/uid/{uid}")
    public ResponseEntity<ApiResponse> getTicketByUid(@PathVariable String uid) {
        try {
            TicketResponse response = ticketService.getTicketByUid(uid);
            return ResponseEntity.ok(new ApiResponse("Get ticket detail by UID success", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Get ticket detail failed: " + e.getMessage(), null));
        }
    }

    // Cancel ticket by ID
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelTicket(@PathVariable int id) {
        try {
            User user = getAuthenticatedUser();
            TicketResponse response = ticketService.cancelTicket(id, user);
            return ResponseEntity.ok(new ApiResponse("Hủy vé thành công!", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("Hủy vé thất bại: " + e.getMessage(), null));
        }
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email;
        if (auth.getPrincipal() instanceof UserDetails ud) {
            email = ud.getUsername();
        } else {
            email = auth.getPrincipal().toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user record not found: " + email));
    }
}
