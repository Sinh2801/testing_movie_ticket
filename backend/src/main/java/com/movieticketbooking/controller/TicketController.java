package com.movieticketbooking.controller;

import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.ticket.CreateTicketRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.PagedResponse;
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

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> filter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String uid    
    )
    {
        PagedResponse<TicketResponse> response = ticketService.filter(page, size, uid);
        return ResponseEntity.ok(new ApiResponse("Filter tickets success", response));
    }
    

    // Create new ticket booking
    @PostMapping("/book")
    public ResponseEntity<ApiResponse> createTicket(@RequestBody @Valid CreateTicketRequest request) 
    {
        User user = getAuthenticatedUser();
        TicketResponse response = ticketService.createTicket(user, request);
        return ResponseEntity.ok(new ApiResponse("Booking ticket success", response));
    }

    // Get ticket history for logged in user
    @GetMapping("/history")
    public ResponseEntity<ApiResponse> getUserTicketHistory() 
    {
        User user = getAuthenticatedUser();
        List<TicketResponse> list = ticketService.getUserTickets(user);
        return ResponseEntity.ok(new ApiResponse("Get user ticket history success", list));
    }

    // Get ticket details by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTicketById(@PathVariable int id) 
    {
        User user = getAuthenticatedUser();
        TicketResponse response = ticketService.getTicketById(id, user);
        return ResponseEntity.ok(new ApiResponse("Get ticket detail success", response));
    }

    // Get ticket details by UID
    @GetMapping("/uid/{uid}")
    public ResponseEntity<ApiResponse> getTicketByUid(@PathVariable String uid) 
    {
        User user = getAuthenticatedUser();
        TicketResponse response = ticketService.getTicketByUid(uid, user);
        return ResponseEntity.ok(new ApiResponse("Get ticket detail by UID success", response));
    }

    // Cancel ticket by ID
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelTicket(@PathVariable int id) 
    {
        User user = getAuthenticatedUser();
        TicketResponse response = ticketService.cancelTicket(id, user);
        return ResponseEntity.ok(new ApiResponse("Hủy vé thành công!", response));
    }

    // get user info from authentication context
    private User getAuthenticatedUser() 
    {
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
