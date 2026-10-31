package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.Room;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.SeatType;
import com.movieticketbooking.model.ShowTime;
import com.movieticketbooking.model.ShowTimeSeat;
import com.movieticketbooking.repository.CinemaRepository;
import com.movieticketbooking.repository.MovieRepository;
import com.movieticketbooking.repository.RoomRepository;
import com.movieticketbooking.repository.SeatRepository;
import com.movieticketbooking.repository.ShowTimeRepository;
import com.movieticketbooking.repository.ShowTimeSeatRepository;
import com.movieticketbooking.request.showtime.CreateShowTimeRequest;
import com.movieticketbooking.response.ShowTimeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowTimeServiceImplCreateShowTimeTest {

    @Mock
    private ShowTimeRepository showTimeRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowTimeSeatRepository showTimeSeatRepository;

    @InjectMocks
    private ShowTimeServiceImpl showTimeService;

    @Test
    @DisplayName("SHOWTIME-01 - Tạo suất chiếu hợp lệ")
    void SHOWTIME_01_createsShowTimeAndSeats() {
        Cinema cinema = cinema();
        Room room = room(cinema);
        Movie movie = movie();
        List<Seat> seats = List.of(seat(10, "A1", room), seat(11, "A2", room), seat(12, "A3", room));
        CreateShowTimeRequest request = request(1, 2, 3, LocalDateTime.now().plusDays(1));
        stubLookups(cinema, room, movie);
        when(seatRepository.findByRoomId(2)).thenReturn(seats);
        when(showTimeRepository.save(any(ShowTime.class)))
                .thenAnswer(invocation -> {
                    ShowTime saved = invocation.getArgument(0);
                    saved.setId(20);
                    return saved;
                });

        ShowTimeResponse response = showTimeService.createShowTime(request);

        ArgumentCaptor<ShowTime> showTimeCaptor = ArgumentCaptor.forClass(ShowTime.class);
        verify(showTimeRepository).save(showTimeCaptor.capture());
        ShowTime savedShowTime = showTimeCaptor.getValue();
        ArgumentCaptor<List> seatsCaptor = ArgumentCaptor.forClass(List.class);
        verify(showTimeSeatRepository).saveAll(seatsCaptor.capture());
        List<?> savedShowTimeSeats = seatsCaptor.getValue();

        assertEquals(3, savedShowTimeSeats.size());
        for (int i = 0; i < seats.size(); i++) {
            ShowTimeSeat showTimeSeat = (ShowTimeSeat) savedShowTimeSeats.get(i);
            assertSame(savedShowTime, showTimeSeat.getShowTime());
            assertSame(seats.get(i), showTimeSeat.getSeat());
            assertEquals(SeatStatus.AVAILABLE, showTimeSeat.getStatus());
            assertNotNull(showTimeSeat.getCreatedAt());
            assertNotNull(showTimeSeat.getUpdatedAt());
        }
        assertEquals(20, response.getId());
        assertEquals(request.getPrice(), response.getPrice());
        assertEquals(request.getStartTime(), response.getStartTime());
        assertEquals(1, response.getCinemaId());
        assertEquals("Cinema 1", response.getCinemaName());
        assertEquals("Address 1", response.getCinemaAddress());
        assertEquals(2, response.getRoomId());
        assertEquals("Room 1", response.getRoomName());
        assertEquals(3, response.getMovieId());
        assertEquals("Movie 1", response.getMovieName());
        assertEquals("movie.png", response.getMovieImageUrl());
        assertEquals(120, response.getDuration());
    }

    @Test
    @DisplayName("SHOWTIME-02 - Cinema không tồn tại")
    void SHOWTIME_02_rejectsMissingCinema() {
        when(cinemaRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> showTimeService.createShowTime(request(99, 2, 3, future())));

        assertEquals("Cinema not found with id: 99", exception.getMessage());
        verify(showTimeRepository, never()).save(any(ShowTime.class));
        verify(showTimeSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("SHOWTIME-03 - Room không tồn tại")
    void SHOWTIME_03_rejectsMissingRoom() {
        Cinema cinema = cinema();
        when(cinemaRepository.findById(1)).thenReturn(Optional.of(cinema));
        when(roomRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> showTimeService.createShowTime(request(1, 99, 3, future())));

        assertEquals("Room not found with id: 99", exception.getMessage());
        verify(showTimeRepository, never()).save(any(ShowTime.class));
        verify(showTimeSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("SHOWTIME-04 - Movie không tồn tại")
    void SHOWTIME_04_rejectsMissingMovie() {
        Cinema cinema = cinema();
        Room room = room(cinema);
        when(cinemaRepository.findById(1)).thenReturn(Optional.of(cinema));
        when(roomRepository.findById(2)).thenReturn(Optional.of(room));
        when(movieRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> showTimeService.createShowTime(request(1, 2, 99, future())));

        assertEquals("Movie not found with id: 99", exception.getMessage());
        verify(showTimeRepository, never()).save(any(ShowTime.class));
        verify(showTimeSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("SHOWTIME-05 - Phòng không có ghế")
    void SHOWTIME_05_acceptsEmptyRoomSeats() {
        Cinema cinema = cinema();
        Room room = room(cinema);
        Movie movie = movie();
        stubLookups(cinema, room, movie);
        when(seatRepository.findByRoomId(2)).thenReturn(List.of());
        when(showTimeRepository.save(any(ShowTime.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Current Behavior: service không có guard cho danh sách ghế rỗng.
        ShowTimeResponse response = showTimeService.createShowTime(
                request(1, 2, 3, future()));

        ArgumentCaptor<List> seatsCaptor = ArgumentCaptor.forClass(List.class);
        verify(showTimeSeatRepository).saveAll(seatsCaptor.capture());
        assertTrueEmpty(seatsCaptor.getValue());
        assertNotNull(response);
        verify(showTimeRepository).save(any(ShowTime.class));
    }

    @Test
    @DisplayName("SHOWTIME-06 - Trùng hoàn toàn thời gian - Known Limitation")
    void SHOWTIME_06_a_sameStartTime() {
        LocalDateTime startTime = future();
        Cinema cinema = cinema();
        Room room = room(cinema);
        Movie movie = movie();
        List<Seat> seats = List.of(seat(10, "A1", room), seat(11, "A2", room));
        stubLookups(cinema, room, movie);
        when(seatRepository.findByRoomId(2)).thenReturn(seats);
        when(showTimeRepository.save(any(ShowTime.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Known Limitation: thiếu kiểm tra xung đột thời gian suất chiếu.
        assertNotNull(showTimeService.createShowTime(request(1, 2, 3, startTime)));
        assertNotNull(showTimeService.createShowTime(request(1, 2, 3, startTime)));

        verify(showTimeRepository, org.mockito.Mockito.times(2)).save(any(ShowTime.class));
        verify(showTimeSeatRepository, org.mockito.Mockito.times(2)).saveAll(any());
        verifyNoMoreInteractions(showTimeRepository);
    }

    @Test
    @DisplayName("SHOWTIME-06 - Giao nhau 30 phút - Known Limitation")
    void SHOWTIME_06_b_overlap30Minutes() {
        LocalDateTime firstStart = future();
        LocalDateTime secondStart = firstStart.plusMinutes(30);
        Cinema cinema = cinema();
        Room room = room(cinema);
        Movie movie = movie();
        List<Seat> seats = List.of(seat(10, "A1", room), seat(11, "A2", room));
        stubLookups(cinema, room, movie);
        when(seatRepository.findByRoomId(2)).thenReturn(seats);
        when(showTimeRepository.save(any(ShowTime.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Known Limitation: thiếu kiểm tra xung đột thời gian suất chiếu.
        assertNotNull(showTimeService.createShowTime(request(1, 2, 3, firstStart)));
        assertNotNull(showTimeService.createShowTime(request(1, 2, 3, secondStart)));

        verify(showTimeRepository, org.mockito.Mockito.times(2)).save(any(ShowTime.class));
        verify(showTimeSeatRepository, org.mockito.Mockito.times(2)).saveAll(any());
        verifyNoMoreInteractions(showTimeRepository);
    }

    private void stubLookups(Cinema cinema, Room room, Movie movie) {
        when(cinemaRepository.findById(1)).thenReturn(Optional.of(cinema));
        when(roomRepository.findById(2)).thenReturn(Optional.of(room));
        when(movieRepository.findById(3)).thenReturn(Optional.of(movie));
    }

    private static CreateShowTimeRequest request(
            int cinemaId, int roomId, int movieId, LocalDateTime startTime) {
        CreateShowTimeRequest request = new CreateShowTimeRequest();
        request.setCinemaId(cinemaId);
        request.setRoomId(roomId);
        request.setMovieId(movieId);
        request.setPrice(BigDecimal.valueOf(100000));
        request.setStartTime(startTime);
        return request;
    }

    private static LocalDateTime future() {
        return LocalDateTime.now().plusDays(1);
    }

    private static Cinema cinema() {
        Cinema cinema = new Cinema();
        cinema.setId(1);
        cinema.setName("Cinema 1");
        cinema.setAddress("Address 1");
        return cinema;
    }

    private static Room room(Cinema cinema) {
        Room room = new Room();
        room.setId(2);
        room.setName("Room 1");
        room.setCinema(cinema);
        return room;
    }

    private static Movie movie() {
        Movie movie = new Movie();
        movie.setId(3);
        movie.setName("Movie 1");
        movie.setImageUrl("movie.png");
        movie.setDuration(120);
        return movie;
    }

    private static Seat seat(int id, String name, Room room) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setSeatName(name);
        seat.setType(SeatType.STANDARD);
        seat.setRoom(room);
        return seat;
    }

    private static void assertTrueEmpty(List<?> values) {
        assertNotNull(values);
        assertEquals(0, values.size());
    }
}
