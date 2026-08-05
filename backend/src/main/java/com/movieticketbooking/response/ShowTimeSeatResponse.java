package com.movieticketbooking.response;

import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowTimeSeatResponse {
    private int id; // ID trong bảng showtime_seats
    private int seatId; // ID trong bảng seats
    private String seatName;
    private SeatType type; // STANDARD, VIP, COUPLE
    private SeatStatus status; // AVAILABLE, BOOKED, RESERVED
}
