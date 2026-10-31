package com.movieticketbooking.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomCreateSeatsTest {

    @Test
    @DisplayName("ROOM-01 - Tạo 1x1 ghế")
    void ROOM_01_createsOneSeat() {
        Room room = room(1, 1);

        List<Seat> seats = room.createSeats();

        assertEquals(1, seats.size());
        assertEquals("A1", seats.get(0).getSeatName());
        assertEquals(SeatType.STANDARD, seats.get(0).getType());
        assertSame(room, seats.get(0).getRoom());
    }

    @Test
    @DisplayName("ROOM-02 - Tạo 26x50 ghế")
    void ROOM_02_createsMaximumEntitySeats() {
        Room room = room(26, 50);

        List<Seat> seats = room.createSeats();

        assertEquals(1300, seats.size());
        assertEquals("A1", seats.get(0).getSeatName());
        assertEquals("Z50", seats.get(seats.size() - 1).getSeatName());
        assertSame(room, seats.get(1299).getRoom());
    }

    @Test
    @DisplayName("ROOM-03 - Rows bằng 0")
    void ROOM_03_rejectsZeroRows() {
        Room room = room(0, 10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, room::createSeats);

        assertEquals("Number of row must be in 1 and 26 ", exception.getMessage());
    }

    @Test
    @DisplayName("ROOM-04 - Rows âm")
    void ROOM_04_rejectsNegativeRows() {
        Room room = room(-1, 10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, room::createSeats);

        assertEquals("Number of row must be in 1 and 26 ", exception.getMessage());
    }

    @Test
    @DisplayName("ROOM-05 - Rows 26")
    void ROOM_05_a_rows26() {
        Room room = room(26, 5);

        List<Seat> seats = room.createSeats();

        assertEquals(130, seats.size());
        assertEquals("Z5", seats.get(seats.size() - 1).getSeatName());
    }

    @Test
    @DisplayName("ROOM-05 - Rows 27")
    void ROOM_05_b_rows27() {
        Room room = room(27, 5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, room::createSeats);

        assertEquals("Number of row must be in 1 and 26 ", exception.getMessage());
    }

    @Test
    @DisplayName("ROOM-06 - Columns bằng 0 - Known Limitation")
    void ROOM_06_a_columns0() {
        Room room = room(5, 0);

        // Known Limitation: createSeats không kiểm tra columns; việc chặn columns <= 0
        // chỉ có ở RoomServiceImpl.
        List<Seat> seats = room.createSeats();

        assertEquals(List.of(), seats);
    }

    @Test
    @DisplayName("ROOM-06 - Columns âm - Known Limitation")
    void ROOM_06_b_columnsNegative() {
        Room room = room(5, -1);

        // Known Limitation: createSeats không kiểm tra columns; việc chặn columns <= 0
        // chỉ có ở RoomServiceImpl.
        List<Seat> seats = room.createSeats();

        assertEquals(List.of(), seats);
    }

    private static Room room(int rows, int columns) {
        Room room = new Room();
        room.setNumberOfRows(rows);
        room.setNumberOfColumns(columns);
        return room;
    }
}
