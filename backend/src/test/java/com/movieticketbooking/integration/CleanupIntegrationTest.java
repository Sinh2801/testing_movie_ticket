package com.movieticketbooking.integration;

import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.ShowTime;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.TicketStatus;
import com.movieticketbooking.model.User;
import com.movieticketbooking.service.impl.TicketCleanupServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho job nhả ghế vé quá hạn (IT-CLEAN-01 .. IT-CLEAN-04).
 * Job được gọi trực tiếp thay vì chờ @Scheduled (mỗi 60 giây) để kết quả xác định.
 */
class CleanupIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TicketCleanupServiceImpl cleanupService;

    private LocalDateTime future() {
        return LocalDateTime.now().plusDays(3);
    }

    private int pendingTicketFor(User user, ShowTime st, int seatIndex) throws Exception {
        return bookViaApi(bearer(user), st, List.of(seatIdAt(st, seatIndex))).path("id").asInt();
    }

    @Test
    @DisplayName("IT-CLEAN-01: Job nhả ghế cho vé PENDING quá 15 phút")
    void it01_releaseExpiredPendingTicket() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketId = pendingTicketFor(user, st, 0);
        backdateTicket(ticketId, LocalDateTime.now().minusMinutes(20));

        cleanupService.releaseExpiredTickets();

        assertEquals(TicketStatus.CANCELLED, ticketEntity(ticketId).getStatus(), "vé chuyển CANCELLED");
        assertEquals(SeatStatus.AVAILABLE, seatStatus(seatId), "ghế RESERVED -> AVAILABLE");
    }

    @Test
    @DisplayName("IT-CLEAN-02: Job không đụng vé PENDING chưa quá 15 phút")
    void it02_keepRecentPendingTicket() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketId = pendingTicketFor(user, st, 0);
        backdateTicket(ticketId, LocalDateTime.now().minusMinutes(5));

        cleanupService.releaseExpiredTickets();

        assertEquals(TicketStatus.PENDING, ticketEntity(ticketId).getStatus(), "vé vẫn PENDING");
        assertEquals(SeatStatus.RESERVED, seatStatus(seatId), "ghế vẫn RESERVED");
    }

    @Test
    @DisplayName("IT-CLEAN-03: Job không đụng vé đã CONFIRMED")
    void it03_keepConfirmedTicket() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketId = pendingTicketFor(user, st, 0);

        // Giả lập vé đã được xác nhận thanh toán và tạo cách đây > 15 phút
        Ticket ticket = ticketEntity(ticketId);
        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        ticketRepository.save(ticket);
        var sts = showTimeSeatRepository.findById(seatId).orElseThrow();
        sts.setStatus(SeatStatus.BOOKED);
        showTimeSeatRepository.save(sts);

        cleanupService.releaseExpiredTickets();

        assertEquals(TicketStatus.CONFIRMED, ticketEntity(ticketId).getStatus(), "vé vẫn CONFIRMED");
        assertEquals(SeatStatus.BOOKED, seatStatus(seatId), "ghế vẫn BOOKED");
    }

    /**
     * Kỳ vọng theo sheet: sau khi job nhả ghế, user khác đặt được ghế đó (HTTP 200).
     *
     * Lưu ý khi phân tích code: TicketCleanupServiceImpl chỉ đổi trạng thái vé và ghế,
     * KHÔNG set cancelledAt trên TicketSeat. TicketServiceImpl.createTicket lại kiểm tra
     * ticket_seats có cancelledAt == null để chặn đặt trùng, nên ghế vẫn bị coi là đã đặt.
     * Test này dự kiến FAIL (HTTP 409) cho đến khi job được sửa.
     */
    @Test
    @DisplayName("IT-CLEAN-04: Đặt lại ghế sau khi job nhả ghế")
    void it04_rebookAfterCleanup() throws Exception {
        User userA = createUser(Role.USER);
        User userB = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketA = pendingTicketFor(userA, st, 0);
        backdateTicket(ticketA, LocalDateTime.now().minusMinutes(20));

        cleanupService.releaseExpiredTickets();
        assertEquals(SeatStatus.AVAILABLE, seatStatus(seatId), "tiền điều kiện: ghế đã được nhả");

        doPost("/api/tickets/book", bookBody(st, List.of(seatId)), bearer(userB))
                .andExpect(status().isOk());

        assertEquals(SeatStatus.RESERVED, seatStatus(seatId), "ghế RESERVED cho B");
    }
}
