package com.movieticketbooking.response;

import com.movieticketbooking.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private int id;
    private String uid;
    private BigDecimal totalPrice;
    private TicketStatus status;
    private LocalDateTime createdAt;

    // Showtime & Movie details
    private int showtimeId;
    private String movieName;
    private String movieImageUrl;
    private String cinemaName;
    private String roomName;
    private LocalDateTime startTime;

    // User details
    private String userName;
    private String userEmail;

    // Seat details
    private List<String> seatNames;
}
