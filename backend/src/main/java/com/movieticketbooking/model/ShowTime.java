package com.movieticketbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "showtimes")
public class ShowTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private BigDecimal price;
	private LocalDateTime startTime;
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cinema_id")
	private Cinema cinema;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id")
	private Room room;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "movie_id")
	private Movie movie;

	//Create show time seat have name EX: A1 - Z10000
//	public List<ShowTimeSeat> createListShowTimeSeats(){
//		int rows = room.getNumberOfRows();
//		int columns = room.getNumberOfColumns();
//		if (rows < 1 && rows > 26)
//			throw new IllegalArgumentException("Number of row must be in 1 and 26 ");
//		List<Seat> seats = new ArrayList<>();
//		int length = rows * columns;
//		int count = 0;
//		char seatLetter = 'A';
//		int seatNumber = 1;
//		while (count < length) {
//			seats.add(new Seat("" + seatLetter + seatNumber, room, this));
//			seatNumber++;
//			count++;
//			if (count >= columns && count % columns == 0) {
//				seatNumber = 1;
//				seatLetter++;
//			}
//
//		}
//		return seats;
//	}

}
