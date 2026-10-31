package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.model.Room;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.SeatType;
import com.movieticketbooking.repository.RoomRepository;
import com.movieticketbooking.repository.SeatRepository;
import com.movieticketbooking.request.room.CreateRoomRequest;
import com.movieticketbooking.response.RoomResponse;
import com.movieticketbooking.service.CinemaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplCreateRoomTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private CinemaService cinemaService;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    @DisplayName("ROOM-07 - Columns bằng 0")
    void ROOM_07_a_columns0() {
        assertRejectsInvalidColumns(0);
    }

    @Test
    @DisplayName("ROOM-07 - Columns bằng 51")
    void ROOM_07_b_columns51() {
        assertRejectsInvalidColumns(51);
    }

    @Test
    @DisplayName("ROOM-07 - Columns bằng 1 hợp lệ")
    void ROOM_07_c_columns1_valid() {
        Cinema cinema = cinema();
        when(cinemaService.findCinemaById(1)).thenReturn(cinema);
        Room savedRoom = room(5, 1, cinema);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        RoomResponse response = roomService.createRoom(request(5, 1));

        assertEquals(5, response.getNumberOfRows());
        assertEquals(1, response.getNumberOfColumns());
        verify(roomRepository).save(any(Room.class));
        verify(seatRepository).saveAll(any());
    }

    @Test
    @DisplayName("ROOM-07 - Columns bằng 50 hợp lệ")
    void ROOM_07_d_columns50_valid() {
        Cinema cinema = cinema();
        when(cinemaService.findCinemaById(1)).thenReturn(cinema);
        Room savedRoom = room(5, 50, cinema);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        RoomResponse response = roomService.createRoom(request(5, 50));

        assertEquals(5, response.getNumberOfRows());
        assertEquals(50, response.getNumberOfColumns());
        verify(roomRepository).save(any(Room.class));
        verify(seatRepository).saveAll(any());
    }

    @Test
    @DisplayName("ROOM-08 - Tạo phòng thành công")
    void ROOM_08_createsRoomAndSeats() {
        Cinema cinema = cinema();
        when(cinemaService.findCinemaById(1)).thenReturn(cinema);
        Room savedRoom = room(4, 5, cinema);
        savedRoom.setId(7);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        RoomResponse response = roomService.createRoom(request(4, 5));

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        Room submittedRoom = roomCaptor.getValue();
        assertEquals("Room 1", submittedRoom.getName());
        assertEquals("Description", submittedRoom.getDescription());
        assertEquals(cinema, submittedRoom.getCinema());

        ArgumentCaptor<List> seatsCaptor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).saveAll(seatsCaptor.capture());
        List<?> seats = seatsCaptor.getValue();
        assertEquals(20, seats.size());
        assertEquals(SeatType.STANDARD, ((Seat) seats.get(0)).getType());
        assertEquals("A1", ((Seat) seats.get(0)).getSeatName());
        assertEquals(savedRoom, ((Seat) seats.get(0)).getRoom());

        assertEquals(7, response.getId());
        assertEquals("Room 1", response.getName());
        assertEquals("Description", response.getDescription());
        assertEquals(4, response.getNumberOfRows());
        assertEquals(5, response.getNumberOfColumns());
        assertNotNull(response);
    }

    private void assertRejectsInvalidColumns(int columns) {
        Cinema cinema = cinema();
        when(cinemaService.findCinemaById(1)).thenReturn(cinema);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roomService.createRoom(request(5, columns)));

        assertEquals("Number of columns must be between 1 and 50", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
        verify(seatRepository, never()).saveAll(any());
    }

    private static CreateRoomRequest request(int rows, int columns) {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("Room 1");
        request.setDescription("Description");
        request.setNumberOfRows(rows);
        request.setNumberOfColumns(columns);
        request.setCinemaId(1);
        return request;
    }

    private static Cinema cinema() {
        Cinema cinema = new Cinema();
        cinema.setId(1);
        cinema.setName("Cinema 1");
        cinema.setAddress("Address");
        return cinema;
    }

    private static Room room(int rows, int columns, Cinema cinema) {
        Room room = new Room();
        room.setName("Room 1");
        room.setDescription("Description");
        room.setNumberOfRows(rows);
        room.setNumberOfColumns(columns);
        room.setCinema(cinema);
        return room;
    }
}
