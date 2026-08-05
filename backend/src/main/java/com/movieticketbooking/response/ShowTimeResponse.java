package com.movieticketbooking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowTimeResponse {
    private int id;
    private BigDecimal price;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;

    // Cinema info
    private int cinemaId;
    private String cinemaName;
    private String cinemaAddress;

    // Room info
    private int roomId;
    private String roomName;

    // Movie info
    private int movieId;
    private String movieName;
    private String movieImageUrl;
    private Integer duration;
}
