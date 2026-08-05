package com.movieticketbooking.request.showtime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateShowTimeRequest {
    @NotNull(message = "Cinema ID is required")
    private Integer cinemaId;

    @NotNull(message = "Room ID is required")
    private Integer roomId;

    @NotNull(message = "Movie ID is required")
    private Integer movieId;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
}
