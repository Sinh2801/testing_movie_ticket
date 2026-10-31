package com.movieticketbooking.service.impl;

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
import com.movieticketbooking.repository.ShowTimeSeatRepository;
import com.movieticketbooking.repository.TicketRepository;
import com.movieticketbooking.repository.TicketSeatRepository;
import com.movieticketbooking.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplCancelTicketTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowTimeSeatRepository showTimeSeatRepository;

    @Mock
    private TicketSeatRepository ticketSeatRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    @DisplayName("TICKET-CANCEL-01 - Hủy vé đúng hạn")
    void TICKET_CANCEL_01_cancelsPendingTicketAndReleasesSeats() {
        User user = user(1);
        ShowTimeSeat seatA1 = showTimeSeat(10, "A1", SeatStatus.RESERVED);
        ShowTimeSeat seatA2 = showTimeSeat(11, "A2", SeatStatus.RESERVED);
        Ticket ticket = ticket(user, TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusDays(1)),
                ticketSeat(seatA1), ticketSeat(seatA2));
        stubTicket(ticket);
        stubTicketSave();

        ticketService.cancelTicket(5, user);

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository).save(seatA1);
        verify(showTimeSeatRepository).save(seatA2);
        verify(ticketSeatRepository).saveAll(ticket.getTicketSeats());
        assertEquals(SeatStatus.AVAILABLE, seatA1.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seatA2.getStatus());
        assertNotNull(ticket.getTicketSeats().get(0).getCancelledAt());
        assertNotNull(ticket.getTicketSeats().get(1).getCancelledAt());
    }

    @Test
    @DisplayName("TICKET-CANCEL-02 - Ticket không tồn tại")
    void TICKET_CANCEL_02_rejectsMissingTicket() {
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.cancelTicket(999, user(1)));

        assertEquals("Ticket not found with id: 999", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-03 - Sai quyền sở hữu")
    void TICKET_CANCEL_03_rejectsDifferentOwner() {
        Ticket ticket = ticket(user(1), TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusDays(1)));
        stubTicket(ticket);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.cancelTicket(5, user(2)));

        assertEquals("You are not authorized to cancel this ticket!", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-04 - currentUser null - Current Behavior")
    void TICKET_CANCEL_04_recordsNullCurrentUserBehavior() {
        Ticket ticket = ticket(user(1), TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusDays(1)));
        stubTicket(ticket);

        // Current Behavior: thiếu guard cho currentUser null.
        assertThrows(NullPointerException.class, () -> ticketService.cancelTicket(5, null));

        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-05 - Vé đã hủy")
    void TICKET_CANCEL_05_rejectsAlreadyCancelledTicket() {
        User user = user(1);
        Ticket ticket = ticket(user, TicketStatus.CANCELLED,
                showTime(LocalDateTime.now().plusDays(1)));
        stubTicket(ticket);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.cancelTicket(5, user));

        assertEquals("Ticket has already been cancelled!", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-06 - Thiếu showtime")
    void TICKET_CANCEL_06_a_noShowTime() {
        User user = user(1);
        Ticket ticket = ticket(user, TicketStatus.PENDING, null);
        stubTicket(ticket);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.cancelTicket(5, user));

        assertEquals("ShowTime information not found for ticket!", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-06 - Thiếu startTime")
    void TICKET_CANCEL_06_b_noStartTime() {
        User user = user(1);
        Ticket ticket = ticket(user, TicketStatus.PENDING, showTime(null));
        stubTicket(ticket);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.cancelTicket(5, user));

        assertEquals("ShowTime information not found for ticket!", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-07 - Showtime còn hơn 2 giờ")
    void TICKET_CANCEL_07_a_farEnough() {
        User user = user(1);
        ShowTimeSeat seat = showTimeSeat(10, "A1", SeatStatus.RESERVED);
        Ticket ticket = ticket(user, TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusHours(2).plusMinutes(5)),
                ticketSeat(seat));
        stubTicket(ticket);
        stubTicketSave();

        ticketService.cancelTicket(5, user);

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository).save(seat);
    }

    @Test
    @DisplayName("TICKET-CANCEL-07 - Showtime còn dưới 2 giờ")
    void TICKET_CANCEL_07_b_tooClose() {
        User user = user(1);
        ShowTimeSeat seat = showTimeSeat(10, "A1", SeatStatus.RESERVED);
        Ticket ticket = ticket(user, TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusHours(1).plusMinutes(55)),
                ticketSeat(seat));
        stubTicket(ticket);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.cancelTicket(5, user));

        assertEquals(
                "Ticket must be cancelled at least 2 hours before the showtime!",
                exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-08 - TicketSeat null - không lưu ghế")
    void TICKET_CANCEL_08_a_ticketSeatsNull() {
        User user = user(1);
        Ticket ticket = ticket(user, TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusDays(1)));
        ticket.setTicketSeats(null);
        stubTicket(ticket);
        stubTicketSave();

        ticketService.cancelTicket(5, user);

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-08 - TicketSeat rỗng - không lưu ghế")
    void TICKET_CANCEL_08_b_emptyList() {
        User user = user(1);
        Ticket ticket = ticket(user, TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusDays(1)));
        ticket.setTicketSeats(new ArrayList<>());
        stubTicket(ticket);
        stubTicketSave();

        ticketService.cancelTicket(5, user);

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
        verify(ticketSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("TICKET-CANCEL-08 - TicketSeat có showTimeSeat null")
    void TICKET_CANCEL_08_c_nullShowTimeSeat() {
        User user = user(1);
        ShowTimeSeat validSeat = showTimeSeat(11, "A2", SeatStatus.RESERVED);
        TicketSeat nullSeatTicket = ticketSeat(null);
        TicketSeat validTicketSeat = ticketSeat(validSeat);
        Ticket ticket = ticket(user, TicketStatus.PENDING,
                showTime(LocalDateTime.now().plusDays(1)),
                nullSeatTicket, validTicketSeat);
        stubTicket(ticket);
        stubTicketSave();

        // Known Limitation: cancelTicket bỏ qua TicketSeat có showTimeSeat == null khi hoàn ghế
        // nhưng mapToResponse không kiểm tra null nên ném NullPointerException; tài liệu thiết kế
        // ghi "không ném exception" không khớp hành vi thật.
        assertThrows(NullPointerException.class, () -> ticketService.cancelTicket(5, user));

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository).save(validSeat);
        verify(ticketSeatRepository).saveAll(ticket.getTicketSeats());
        assertEquals(SeatStatus.AVAILABLE, validSeat.getStatus());
    }

    private void stubTicket(Ticket ticket) {
        when(ticketRepository.findById(5)).thenReturn(Optional.of(ticket));
    }

    private void stubTicketSave() {
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Ticket ticket(User user, TicketStatus status, ShowTime showTime,
                                 TicketSeat... ticketSeats) {
        Ticket ticket = new Ticket();
        ticket.setId(5);
        ticket.setUid("TIC-00000001");
        ticket.setUser(user);
        ticket.setStatus(status);
        ticket.setShowTime(showTime);
        ticket.setTicketSeats(new ArrayList<>(List.of(ticketSeats)));
        return ticket;
    }

    private static TicketSeat ticketSeat(ShowTimeSeat showTimeSeat) {
        TicketSeat ticketSeat = new TicketSeat();
        ticketSeat.setShowTimeSeat(showTimeSeat);
        return ticketSeat;
    }

    private static ShowTime showTime(LocalDateTime startTime) {
        ShowTime showTime = new ShowTime();
        showTime.setId(1);
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
        return showTimeSeat;
    }

    private static User user(int id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@mail.com");
        user.setRole(Role.USER);
        return user;
    }
}
