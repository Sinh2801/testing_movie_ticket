package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceAlreadyExistException;
import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.ShowTime;
import com.movieticketbooking.model.ShowTimeSeat;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.TicketSeat;
import com.movieticketbooking.model.TicketStatus;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.ShowTimeRepository;
import com.movieticketbooking.repository.ShowTimeSeatRepository;
import com.movieticketbooking.repository.TicketRepository;
import com.movieticketbooking.repository.TicketSeatRepository;
import com.movieticketbooking.request.ticket.CreateTicketRequest;
import com.movieticketbooking.response.TicketResponse;
import com.movieticketbooking.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplCreateTicketTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowTimeRepository showTimeRepository;

    @Mock
    private ShowTimeSeatRepository showTimeSeatRepository;

    @Mock
    private TicketSeatRepository ticketSeatRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    @DisplayName("TICKET-BOOK-01 - Đặt 1 ghế còn trống")
    void TICKET_BOOK_01_booksOneAvailableSeat() {
        User user = user();
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        ShowTimeSeat seat = showTimeSeat(10, "A10", SeatStatus.AVAILABLE);
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(10)).thenReturn(Optional.of(seat));
        stubSaves();

        TicketResponse response = ticketService.createTicket(user, request(1, 10));

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        assertEquals(TicketStatus.PENDING, savedTicket.getStatus());
        assertEquals(BigDecimal.valueOf(100000), savedTicket.getTotalPrice());
        assertEquals(user, savedTicket.getUser());

        ArgumentCaptor<List> ticketSeatsCaptor = ArgumentCaptor.forClass(List.class);
        verify(ticketSeatRepository).saveAll(ticketSeatsCaptor.capture());
        List<?> savedTicketSeats = ticketSeatsCaptor.getValue();
        assertEquals(1, savedTicketSeats.size());
        TicketSeat ticketSeat = (TicketSeat) savedTicketSeats.get(0);
        assertEquals(savedTicket, ticketSeat.getTicket());
        assertEquals(seat, ticketSeat.getShowTimeSeat());

        verify(showTimeSeatRepository).saveAll(List.of(seat));
        assertEquals(SeatStatus.RESERVED, seat.getStatus());
        assertEquals(BigDecimal.valueOf(100000), response.getTotalPrice());
        assertEquals(List.of("A10"), response.getSeatNames());
    }

    @Test
    @DisplayName("TICKET-BOOK-02 - Đặt nhiều ghế")
    void TICKET_BOOK_02_booksMultipleSeats() {
        User user = user();
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        ShowTimeSeat seat10 = showTimeSeat(10, "A10", SeatStatus.AVAILABLE);
        ShowTimeSeat seat11 = showTimeSeat(11, "A11", SeatStatus.AVAILABLE);
        ShowTimeSeat seat12 = showTimeSeat(12, "A12", SeatStatus.AVAILABLE);
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(10)).thenReturn(Optional.of(seat10));
        when(showTimeSeatRepository.findByIdWithLock(11)).thenReturn(Optional.of(seat11));
        when(showTimeSeatRepository.findByIdWithLock(12)).thenReturn(Optional.of(seat12));
        stubSaves();

        TicketResponse response = ticketService.createTicket(user, request(1, 10, 11, 12));

        assertEquals(BigDecimal.valueOf(300000), response.getTotalPrice());
        ArgumentCaptor<List> ticketSeatsCaptor = ArgumentCaptor.forClass(List.class);
        verify(ticketSeatRepository).saveAll(ticketSeatsCaptor.capture());
        assertEquals(3, ticketSeatsCaptor.getValue().size());
        verify(showTimeSeatRepository).saveAll(List.of(seat10, seat11, seat12));
        assertEquals(SeatStatus.RESERVED, seat10.getStatus());
        assertEquals(SeatStatus.RESERVED, seat11.getStatus());
        assertEquals(SeatStatus.RESERVED, seat12.getStatus());
    }

    @Test
    @DisplayName("TICKET-BOOK-03 - Showtime không tồn tại")
    void TICKET_BOOK_03_rejectsMissingShowTime() {
        when(showTimeRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.createTicket(user(), request(999, 10)));

        assertEquals("ShowTime not found with id: 999", exception.getMessage());
        verifyNoInteractions(showTimeSeatRepository);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICKET-BOOK-04 - Showtime có startTime null")
    void TICKET_BOOK_04_rejectsNullStartTime() {
        ShowTime showTime = showTime(1, null);
        when(showTimeRepository.findById(1)).thenReturn(Optional.of(showTime));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(user(), request(1, 10)));

        assertEquals(
                "Không thể đặt vé cho suất chiếu đã qua hoặc không hợp lệ!",
                exception.getMessage());
        verifyNoInteractions(showTimeSeatRepository);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICKET-BOOK-05 - Showtime đã diễn ra")
    void TICKET_BOOK_05_rejectsPastShowTime() {
        ShowTime showTime = showTime(1, LocalDateTime.now().minusMinutes(10));
        when(showTimeRepository.findById(1)).thenReturn(Optional.of(showTime));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(user(), request(1, 10)));

        assertEquals(
                "Không thể đặt vé cho suất chiếu đã qua hoặc không hợp lệ!",
                exception.getMessage());
        verifyNoInteractions(showTimeSeatRepository);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICKET-BOOK-05 - Showtime tương lai gần")
    void TICKET_BOOK_05_acceptsNearFutureShowTime() {
        ShowTime showTime = showTime(1, LocalDateTime.now().plusMinutes(10));
        ShowTimeSeat seat = showTimeSeat(10, "A10", SeatStatus.AVAILABLE);
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(10)).thenReturn(Optional.of(seat));
        stubSaves();

        assertNotNull(ticketService.createTicket(user(), request(1, 10)));
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICKET-BOOK-06 - Seat không tồn tại")
    void TICKET_BOOK_06_rejectsMissingSeat() {
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(777)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.createTicket(user(), request(1, 777)));

        assertEquals("ShowTimeSeat not found with id: 777", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICKET-BOOK-07 - Seat BOOKED")
    void TICKET_BOOK_07_booked() {
        assertRejectsUnavailableSeat(SeatStatus.BOOKED);
    }

    @Test
    @DisplayName("TICKET-BOOK-07 - Seat RESERVED")
    void TICKET_BOOK_07_reserved() {
        assertRejectsUnavailableSeat(SeatStatus.RESERVED);
    }

    @Test
    @DisplayName("TICKET-BOOK-08 - Seat có booking đang active")
    void TICKET_BOOK_08_rejectsActiveBooking() {
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        ShowTimeSeat seat = showTimeSeat(10, "A10", SeatStatus.AVAILABLE);
        TicketSeat activeTicketSeat = new TicketSeat();
        activeTicketSeat.setCancelledAt(null);
        seat.setTicketSeats(List.of(activeTicketSeat));
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(10)).thenReturn(Optional.of(seat));

        ResourceAlreadyExistException exception = assertThrows(
                ResourceAlreadyExistException.class,
                () -> ticketService.createTicket(user(), request(1, 10)));

        assertEquals("Seat A10 has already been booked!", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICKET-BOOK-09 - Ghế có TicketSeat đã hủy")
    void TICKET_BOOK_09_rebooksSeatWithCancelledBookings() {
        User user = user();
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        ShowTimeSeat seat = showTimeSeat(10, "A10", SeatStatus.AVAILABLE);
        TicketSeat cancelledTicketSeat = new TicketSeat();
        cancelledTicketSeat.setCancelledAt(LocalDateTime.now().minusHours(1));
        seat.setTicketSeats(List.of(cancelledTicketSeat));
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(10)).thenReturn(Optional.of(seat));
        stubSaves();

        TicketResponse response = ticketService.createTicket(user, request(1, 10));

        assertEquals(TicketStatus.PENDING, response.getStatus());
        assertEquals(SeatStatus.RESERVED, seat.getStatus());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TICKET-BOOK-10 - Danh sách ghế rỗng - Known Limitation")
    void TICKET_BOOK_10_a_emptyList() {
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        stubValidShowTime(showTime);
        stubSaves();

        // Known Limitation: createTicket() không có guard cho danh sách ghế rỗng.
        TicketResponse response = ticketService.createTicket(user(), request(1));

        assertEquals(TicketStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.ZERO, response.getTotalPrice());
        assertTrue(response.getSeatNames().isEmpty());
        verify(ticketSeatRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("TICKET-BOOK-10 - Trùng ID ghế - Known Limitation")
    void TICKET_BOOK_10_b_duplicateIds() {
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(10))
                .thenAnswer(invocation -> Optional.of(
                        showTimeSeat(10, "A10", SeatStatus.AVAILABLE)));
        stubSaves();

        // Known Limitation: createTicket() không có guard cho ID ghế trùng.
        TicketResponse response = ticketService.createTicket(user(), request(1, 10, 10));

        assertEquals(BigDecimal.valueOf(200000), response.getTotalPrice());
        ArgumentCaptor<List> ticketSeatsCaptor = ArgumentCaptor.forClass(List.class);
        verify(ticketSeatRepository).saveAll(ticketSeatsCaptor.capture());
        assertEquals(2, ticketSeatsCaptor.getValue().size());
        assertEquals(10, ((TicketSeat) ticketSeatsCaptor.getValue().get(0))
                .getShowTimeSeat().getId());
        assertEquals(10, ((TicketSeat) ticketSeatsCaptor.getValue().get(1))
                .getShowTimeSeat().getId());
    }

    private void assertRejectsUnavailableSeat(SeatStatus status) {
        ShowTime showTime = showTime(1, LocalDateTime.now().plusDays(1));
        ShowTimeSeat seat = showTimeSeat(10, "A10", status);
        stubValidShowTime(showTime);
        when(showTimeSeatRepository.findByIdWithLock(10)).thenReturn(Optional.of(seat));

        ResourceAlreadyExistException exception = assertThrows(
                ResourceAlreadyExistException.class,
                () -> ticketService.createTicket(user(), request(1, 10)));

        assertEquals("Seat A10 is not available!", exception.getMessage());
        assertEquals(status, seat.getStatus());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    private void stubValidShowTime(ShowTime showTime) {
        when(showTimeRepository.findById(showTime.getId())).thenReturn(Optional.of(showTime));
    }

    private void stubSaves() {
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static CreateTicketRequest request(int showtimeId, Integer... seatIds) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setShowtimeId(showtimeId);
        request.setShowtimeSeatIds(Arrays.asList(seatIds));
        return request;
    }

    private static ShowTime showTime(int id, LocalDateTime startTime) {
        ShowTime showTime = new ShowTime();
        showTime.setId(id);
        showTime.setPrice(BigDecimal.valueOf(100000));
        showTime.setStartTime(startTime);
        return showTime;
    }

    private static ShowTimeSeat showTimeSeat(int id, String seatName, SeatStatus status) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setSeatName(seatName);

        ShowTimeSeat showTimeSeat = new ShowTimeSeat();
        showTimeSeat.setId(id);
        showTimeSeat.setSeat(seat);
        showTimeSeat.setStatus(status);
        showTimeSeat.setTicketSeats(new ArrayList<>());
        return showTimeSeat;
    }

    private static User user() {
        User user = new User();
        user.setId(1);
        user.setEmail("user@mail.com");
        user.setName("Nguyen Van A");
        user.setRole(Role.USER);
        return user;
    }
}
