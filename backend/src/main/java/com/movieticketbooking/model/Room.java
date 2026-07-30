package com.movieticketbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private int numberOfColumns;
	private int numberOfRows;
	private String description;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cinema_id")
	private Cinema cinema;

	public List<Seat> createSeats(){
		int rows = this.getNumberOfRows();
		int columns = this.getNumberOfColumns();
		if (rows < 1 || rows > 26) throw new IllegalArgumentException("Number of row must be in 1 and 26 ");
		List<Seat> seats = new ArrayList<>();
		int length = rows * columns;
		int count = 0;
		char seatLetter = 'A';
		int seatNumber = 1;
		Seat seat = null;
		while (count < length) {
			seat = new Seat();
			seat.setSeatName("" + seatLetter + seatNumber);
			seat.setRoom(this);
			seat.setType(SeatType.STANDARD);
			seats.add(seat);
			seatNumber++;
			count++;
			if (count >= columns && count % columns == 0) {
				seatNumber = 1;
				seatLetter++;
			}

		}
		return seats;
	}
}
