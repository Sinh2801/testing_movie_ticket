package com.movieticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.movieticketbooking.model.PaymentStatus;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Luồng end-to-end (IT-FLOW-01, IT-FLOW-02): Auth -> Ticket -> Payment -> Cleanup -> DB.
 */
class FlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TicketCleanupServiceImpl cleanupService;

    @Test
    @DisplayName("IT-FLOW-01: Luồng đặt vé trọn vẹn")
    void it01_fullBookingFlow() throws Exception {
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(LocalDateTime.now().plusDays(3), 1);
        int seatId = seatIdAt(st, 0);

        // 1. Đăng ký và đăng nhập USER (qua API)
        String email = uniqueEmail();
        MvcResult register = doPost("/api/auth/register", Map.of(
                        "name", "Flow User",
                        "email", email,
                        "phoneNumber", "0912345678",
                        "password", PASSWORD,
                        "confirmPassword", PASSWORD), null)
                .andExpect(status().isOk())
                .andReturn();
        String userToken = "Bearer " + readJson(register).path("data").path("token").asText();

        // 2. Đặt vé
        MvcResult book = doPost("/api/tickets/book", bookBody(st, List.of(seatId)), userToken)
                .andExpect(status().isOk())
                .andReturn();
        int ticketId = readJson(book).path("data").path("id").asInt();

        // 3. Tạo thanh toán local
        doPost("/api/payments/local/create", Map.of("ticketId", ticketId), userToken)
                .andExpect(status().isOk());

        // 4. ADMIN xác nhận thanh toán
        doPostNoBody("/api/payments/local/confirm?ticketId=" + ticketId, bearer(admin))
                .andExpect(status().isOk());

        // 5. USER xem lịch sử vé
        MvcResult history = doGet("/api/tickets/history", userToken)
                .andExpect(status().isOk())
                .andReturn();
        JsonNode list = readJson(history).path("data");
        assertEquals("CONFIRMED", list.get(0).path("status").asText(), "vé CONFIRMED trong lịch sử");

        // Cuối luồng: payment SUCCESS, ghế BOOKED, email gửi đúng 1 lần
        assertEquals(PaymentStatus.SUCCESS, paymentRepository.findByTicketId(ticketId).orElseThrow().getStatus());
        assertEquals(TicketStatus.CONFIRMED, ticketEntity(ticketId).getStatus());
        assertEquals(SeatStatus.BOOKED, seatStatus(seatId));
        verify(emailService, times(1)).sendTicketConfirmationEmail(any(Ticket.class));
    }

    @Test
    @DisplayName("IT-FLOW-02: Luồng đặt vé nhưng không thanh toán")
    void it02_bookingWithoutPaymentExpires() throws Exception {
        User user = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(LocalDateTime.now().plusDays(3), 1);
        int seatId = seatIdAt(st, 0);

        // 1. Đặt vé, tạo payment local
        int ticketId = bookViaApi(bearer(user), st, List.of(seatId)).path("id").asInt();
        doPost("/api/payments/local/create", Map.of("ticketId", ticketId), bearer(user))
                .andExpect(status().isOk());

        // 2. Đẩy created_at của vé lùi 20 phút
        backdateTicket(ticketId, LocalDateTime.now().minusMinutes(20));

        // 3. Chạy job nhả ghế
        cleanupService.releaseExpiredTickets();
        assertEquals(TicketStatus.CANCELLED, ticketEntity(ticketId).getStatus(), "vé CANCELLED");
        assertEquals(SeatStatus.AVAILABLE, seatStatus(seatId), "ghế AVAILABLE");

        // 4. ADMIN thử xác nhận thanh toán -> bị từ chối, payment không thành SUCCESS
        doPostNoBody("/api/payments/local/confirm?ticketId=" + ticketId, bearer(admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Ticket status is not PENDING")));

        assertNotEquals(PaymentStatus.SUCCESS,
                paymentRepository.findByTicketId(ticketId).orElseThrow().getStatus(),
                "payment không được chuyển SUCCESS");
    }
}
