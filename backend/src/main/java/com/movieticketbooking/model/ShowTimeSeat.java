package com.movieticketbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "showtime_seats")
public class ShowTimeSeat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Enumerated(EnumType.STRING)
	private SeatStatus status;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "seat_id")
	private Seat seat;
	@OneToMany(mappedBy = "showTimeSeat")
	private List<TicketSeat> ticketSeats = new ArrayList<>();
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "showtime_id")
	private ShowTime showTime;
}
