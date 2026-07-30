package com.movieticketbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tickets")
public class Ticket {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String uid;

	private BigDecimal totalPrice;
	private TicketStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "showtime_id")
	private ShowTime showTime;
	@OneToMany(mappedBy = "ticket")
	private List<TicketSeat> ticketSeats = new ArrayList<>();
}
