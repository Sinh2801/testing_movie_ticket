package com.movieticketbooking.service.impl;

import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.ShowTimeSeat;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.TicketSeat;
import com.movieticketbooking.model.TicketStatus;
import com.movieticketbooking.repository.ShowTimeSeatRepository;
import com.movieticketbooking.repository.TicketRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketCleanupServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowTimeSeatRepository showTimeSeatRepository;

    @InjectMocks
    private TicketCleanupServiceImpl cleanupService;

    @Test
    @DisplayName("CLEAN-01 - Không có vé hết hạn")
    void CLEAN_01_returnsEarlyWhenNoExpiredTickets() {
        when(ticketRepository.findByStatusAndCreatedAtBefore(
                any(TicketStatus.class), any(LocalDateTime.class))).thenReturn(List.of());

        cleanupService.releaseExpiredTickets();

        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
    }

    @Test
    @DisplayName("CLEAN-02 - Hủy một vé hết hạn có ghế RESERVED")
    void CLEAN_02_cancelsExpiredTicketAndReleasesReservedSeat() {
        ShowTimeSeat showTimeSeat = showTimeSeat(SeatStatus.RESERVED);
        Ticket ticket = expiredTicket(ticketSeat(showTimeSeat));
        stubExpiredTickets(ticket);

        cleanupService.releaseExpiredTickets();

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        assertEquals(SeatStatus.AVAILABLE, showTimeSeat.getStatus());
        assertNotNull(ticket.getUpdatedAt());
        assertNotNull(showTimeSeat.getUpdatedAt());
        verify(showTimeSeatRepository).save(showTimeSeat);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("CLEAN-03 - Hủy nhiều vé hết hạn")
    void CLEAN_03_cancelsAllExpiredTicketsAndReleasesAllReservedSeats() {
        ShowTimeSeat seat1 = showTimeSeat(SeatStatus.RESERVED);
        ShowTimeSeat seat2 = showTimeSeat(SeatStatus.RESERVED);
        ShowTimeSeat seat3 = showTimeSeat(SeatStatus.RESERVED);
        ShowTimeSeat seat4 = showTimeSeat(SeatStatus.RESERVED);
        ShowTimeSeat seat5 = showTimeSeat(SeatStatus.RESERVED);
        Ticket ticket1 = expiredTicket(ticketSeat(seat1));
        Ticket ticket2 = expiredTicket(ticketSeat(seat2), ticketSeat(seat3));
        Ticket ticket3 = expiredTicket(ticketSeat(seat4), ticketSeat(seat5));
        when(ticketRepository.findByStatusAndCreatedAtBefore(
                any(TicketStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(ticket1, ticket2, ticket3));

        cleanupService.releaseExpiredTickets();

        assertEquals(TicketStatus.CANCELLED, ticket1.getStatus());
        assertEquals(TicketStatus.CANCELLED, ticket2.getStatus());
        assertEquals(TicketStatus.CANCELLED, ticket3.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat1.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat2.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat3.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat4.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat5.getStatus());
        verify(ticketRepository, times(3)).save(any(Ticket.class));
        verify(showTimeSeatRepository, times(5)).save(any(ShowTimeSeat.class));
    }

    @Test
    @DisplayName("CLEAN-04(a) - Ghế AVAILABLE không được lưu")
    void CLEAN_04_a_availableSeat() {
        ShowTimeSeat showTimeSeat = showTimeSeat(SeatStatus.AVAILABLE);
        Ticket ticket = expiredTicket(ticketSeat(showTimeSeat));
        stubExpiredTickets(ticket);

        cleanupService.releaseExpiredTickets();

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        assertEquals(SeatStatus.AVAILABLE, showTimeSeat.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
    }

    @Test
    @DisplayName("CLEAN-04(b) - Ghế BOOKED không được lưu")
    void CLEAN_04_b_bookedSeat() {
        ShowTimeSeat showTimeSeat = showTimeSeat(SeatStatus.BOOKED);
        Ticket ticket = expiredTicket(ticketSeat(showTimeSeat));
        stubExpiredTickets(ticket);

        cleanupService.releaseExpiredTickets();

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        assertEquals(SeatStatus.BOOKED, showTimeSeat.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
    }

    @Test
    @DisplayName("CLEAN-05(a) - ticketSeats null")
    void CLEAN_05_a_ticketSeatsNull() {
        Ticket ticket = expiredTicket();
        ticket.setTicketSeats(null);
        stubExpiredTickets(ticket);

        assertDoesNotThrow(() -> cleanupService.releaseExpiredTickets());

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
    }

    @Test
    @DisplayName("CLEAN-05(b) - showTimeSeat null")
    void CLEAN_05_b_showTimeSeatNull() {
        Ticket ticket = expiredTicket(ticketSeat(null));
        stubExpiredTickets(ticket);

        assertDoesNotThrow(() -> cleanupService.releaseExpiredTickets());

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
    }

    @Test
    @DisplayName("CLEAN-06 - Ngưỡng truy vấn 15 phút")
    void CLEAN_06_queriesPendingTicketsBeforeFifteenMinutes() {
        when(ticketRepository.findByStatusAndCreatedAtBefore(
                any(TicketStatus.class), any(LocalDateTime.class))).thenReturn(List.of());
        LocalDateTime t0 = LocalDateTime.now();

        cleanupService.releaseExpiredTickets();

        LocalDateTime t1 = LocalDateTime.now();
        ArgumentCaptor<TicketStatus> statusCaptor =
                ArgumentCaptor.forClass(TicketStatus.class);
        ArgumentCaptor<LocalDateTime> timeCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        verify(ticketRepository).findByStatusAndCreatedAtBefore(
                statusCaptor.capture(), timeCaptor.capture());

        LocalDateTime threshold = timeCaptor.getValue();
        assertEquals(TicketStatus.PENDING, statusCaptor.getValue());
        assertFalse(threshold.isBefore(t0.minusMinutes(15)));
        assertFalse(threshold.isAfter(t1.minusMinutes(15)));

        // Truy vấn dùng Before nghiêm ngặt; xác nhận chính xác tại biên cần inject Clock.
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(showTimeSeatRepository, never()).save(any(ShowTimeSeat.class));
    }

    private void stubExpiredTickets(Ticket... tickets) {
        when(ticketRepository.findByStatusAndCreatedAtBefore(
                any(TicketStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(tickets));
    }

    private static Ticket expiredTicket(TicketSeat... ticketSeats) {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        ticket.setTicketSeats(new ArrayList<>(List.of(ticketSeats)));
        for (TicketSeat ticketSeat : ticketSeats) {
            if (ticketSeat != null) {
                ticketSeat.setTicket(ticket);
            }
        }
        return ticket;
    }

    private static TicketSeat ticketSeat(ShowTimeSeat showTimeSeat) {
        TicketSeat ticketSeat = new TicketSeat();
        ticketSeat.setShowTimeSeat(showTimeSeat);
        return ticketSeat;
    }

    private static ShowTimeSeat showTimeSeat(SeatStatus status) {
        ShowTimeSeat showTimeSeat = new ShowTimeSeat();
        showTimeSeat.setStatus(status);
        return showTimeSeat;
    }
}
