package com.movieticketbooking.request.ticket;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateTicketRequest {
    @NotNull(message = "Showtime ID is required")
    private Integer showtimeId;

    @NotEmpty(message = "Seat list cannot be empty")
    private List<Integer> showtimeSeatIds;
}
