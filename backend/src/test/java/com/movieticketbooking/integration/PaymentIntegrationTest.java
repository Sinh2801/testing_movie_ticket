package com.movieticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.movieticketbooking.model.Payment;
import com.movieticketbooking.model.PaymentMethod;
import com.movieticketbooking.model.PaymentStatus;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.ShowTime;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.TicketStatus;
import com.movieticketbooking.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho module Payment (IT-PAY-01 .. IT-PAY-10).
 * Luồng: Controller -> PaymentService -> PaymentRepository / TicketService -> MySQL, EmailService (mock).
 */
class PaymentIntegrationTest extends IntegrationTestBase {

    private static final String CREATE = "/api/payments/local/create";
    private static final String CONFIRM = "/api/payments/local/confirm";

    private LocalDateTime future() {
        return LocalDateTime.now().plusDays(3);
    }

    /** Tạo vé PENDING qua API cho user và trả về id vé. */
    private int pendingTicket(User user, ShowTime st, int seatIndex) throws Exception {
        return bookViaApi(bearer(user), st, List.of(seatIdAt(st, seatIndex))).path("id").asInt();
    }

    private Payment paymentOf(int ticketId) {
        return paymentRepository.findByTicketId(ticketId).orElseThrow();
    }

    // =====================================================================
    //  Tạo thanh toán (IT-PAY-01 .. 04)
    // =====================================================================

    @Test
    @DisplayName("IT-PAY-01: Tạo thanh toán local cho vé PENDING")
    void it01_createLocalPayment() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        JsonNode ticketData = bookViaApi(bearer(user), st, List.of(seatIdAt(st, 0)));
        int ticketId = ticketData.path("id").asInt();

        MvcResult result = doPost(CREATE, Map.of("ticketId", ticketId), bearer(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Create Local payment success"))
                .andReturn();

        JsonNode data = readJson(result).path("data");
        String transactionId = data.path("transactionId").asText();
        String paymentUrl = data.path("paymentUrl").asText();
        assertTrue(paymentUrl.contains("ticketId=" + ticketId), "paymentUrl chứa ticketId");
        assertTrue(paymentUrl.contains("transactionId=" + transactionId), "paymentUrl chứa transactionId");

        Payment payment = paymentOf(ticketId);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(PaymentMethod.LOCAL, payment.getMethod());
        assertEquals(0, payment.getAmount().compareTo(DEFAULT_PRICE), "amount = totalPrice của vé");
        assertEquals(ticketData.path("uid").asText(), payment.getPaymentCode(), "paymentCode = uid vé");
    }

    @Test
    @DisplayName("IT-PAY-02: Tạo lại thanh toán cho cùng một vé")
    void it02_createPaymentTwice() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int ticketId = pendingTicket(user, st, 0);

        String tx1 = readJson(doPost(CREATE, Map.of("ticketId", ticketId), bearer(user))
                .andExpect(status().isOk()).andReturn()).path("data").path("transactionId").asText();
        String tx2 = readJson(doPost(CREATE, Map.of("ticketId", ticketId), bearer(user))
                .andExpect(status().isOk()).andReturn()).path("data").path("transactionId").asText();

        assertEquals(1, paymentRepository.count(), "chỉ 1 payment cho vé (cập nhật bản ghi cũ)");
        assertNotEquals(tx1, tx2, "transactionId được tạo mới");
        assertEquals(tx2, paymentOf(ticketId).getTransactionId());
    }

    @Test
    @DisplayName("IT-PAY-03: Tạo thanh toán cho vé không tồn tại")
    void it03_createPaymentUnknownTicket() throws Exception {
        User user = createUser(Role.USER);

        doPost(CREATE, Map.of("ticketId", 999999), bearer(user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found with id: 999999"));
    }

    @Test
    @DisplayName("IT-PAY-04: Tạo thanh toán khi chưa đăng nhập")
    void it04_createPaymentWithoutToken() throws Exception {
        int httpStatus = doPost(CREATE, Map.of("ticketId", 1), null)
                .andReturn().getResponse().getStatus();

        assertTrue(httpStatus == 401 || httpStatus == 403,
                "phải bị chặn với 401 hoặc 403, nhận được: " + httpStatus);
        assertEquals(0, paymentRepository.count());
    }

    // =====================================================================
    //  Xác nhận thanh toán (IT-PAY-05 .. 09)
    // =====================================================================

    @Test
    @DisplayName("IT-PAY-05: ADMIN xác nhận thanh toán local thành công")
    void it05_adminConfirmsPayment() throws Exception {
        User user = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketId = pendingTicket(user, st, 0);
        doPost(CREATE, Map.of("ticketId", ticketId), bearer(user)).andExpect(status().isOk());

        doPostNoBody(CONFIRM + "?ticketId=" + ticketId, bearer(admin))
                .andExpect(status().isOk())
                // Message ngoài là "Confirm Local payment success" (PaymentController); câu trong sheet nằm ở data.message
                .andExpect(jsonPath("$.data.message").value("Local Payment successfully confirmed"));

        Payment payment = paymentOf(ticketId);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNotNull(payment.getPaidAt(), "paidAt có giá trị");
        assertEquals(TicketStatus.CONFIRMED, ticketEntity(ticketId).getStatus());
        assertEquals(SeatStatus.BOOKED, seatStatus(seatId), "ghế chuyển BOOKED");
        verify(emailService, times(1)).sendTicketConfirmationEmail(any(Ticket.class));
    }

    @Test
    @DisplayName("IT-PAY-06: USER thường xác nhận thanh toán local")
    void it06_userCannotConfirm() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int ticketId = pendingTicket(user, st, 0);
        doPost(CREATE, Map.of("ticketId", ticketId), bearer(user)).andExpect(status().isOk());

        doPostNoBody(CONFIRM + "?ticketId=" + ticketId, bearer(user))
                .andExpect(status().isForbidden());

        assertEquals(PaymentStatus.PENDING, paymentOf(ticketId).getStatus(), "payment vẫn PENDING");
        assertEquals(TicketStatus.PENDING, ticketEntity(ticketId).getStatus(), "vé vẫn PENDING");
    }

    @Test
    @DisplayName("IT-PAY-07: Xác nhận khi chưa có payment")
    void it07_confirmWithoutPayment() throws Exception {
        User user = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(future(), 1);
        int ticketId = pendingTicket(user, st, 0); // chưa tạo payment

        doPostNoBody(CONFIRM + "?ticketId=" + ticketId, bearer(admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Payment not found for ticket")));
    }

    @Test
    @DisplayName("IT-PAY-08: Xác nhận thanh toán lần 2")
    void it08_confirmTwice() throws Exception {
        User user = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketId = pendingTicket(user, st, 0);
        doPost(CREATE, Map.of("ticketId", ticketId), bearer(user)).andExpect(status().isOk());
        doPostNoBody(CONFIRM + "?ticketId=" + ticketId, bearer(admin)).andExpect(status().isOk());

        doPostNoBody(CONFIRM + "?ticketId=" + ticketId, bearer(admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ticket status is not PENDING, cannot confirm payment!"));

        assertEquals(SeatStatus.BOOKED, seatStatus(seatId), "ghế vẫn BOOKED");
        verify(emailService, times(1)).sendTicketConfirmationEmail(any(Ticket.class));
    }

    @Test
    @DisplayName("IT-PAY-09: Xác nhận thanh toán cho vé đã bị hủy (kiểm tra rollback)")
    void it09_confirmCancelledTicketRollsBack() throws Exception {
        User user = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketId = pendingTicket(user, st, 0);
        doPost(CREATE, Map.of("ticketId", ticketId), bearer(user)).andExpect(status().isOk());

        // Người dùng hủy vé trước khi thanh toán
        doPostNoBody("/api/tickets/" + ticketId + "/cancel", bearer(user)).andExpect(status().isOk());
        assertEquals(PaymentStatus.PENDING, paymentOf(ticketId).getStatus());

        doPostNoBody(CONFIRM + "?ticketId=" + ticketId, bearer(admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ticket status is not PENDING, cannot confirm payment!"));

        // Rollback: payment không được chuyển SUCCESS, ghế không bị đánh BOOKED
        assertEquals(PaymentStatus.PENDING, paymentOf(ticketId).getStatus(), "payment không được chuyển SUCCESS");
        assertNull(paymentOf(ticketId).getPaidAt(), "không có paidAt");
        assertEquals(SeatStatus.AVAILABLE, seatStatus(seatId), "ghế không bị đánh BOOKED");
        verify(emailService, never()).sendTicketConfirmationEmail(any(Ticket.class));
    }

    // =====================================================================
    //  Lọc thanh toán (IT-PAY-10)
    // =====================================================================

    @Test
    @DisplayName("IT-PAY-10: ADMIN lọc danh sách payment / USER bị chặn")
    void it10_filterPaymentsAdminOnly() throws Exception {
        User user = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(future(), 2);
        for (int i = 0; i < 2; i++) {
            int ticketId = pendingTicket(user, st, i);
            doPost(CREATE, Map.of("ticketId", ticketId), bearer(user)).andExpect(status().isOk());
        }

        doGet("/api/payments/filter?page=1&size=10&paymentCode=TIC", bearer(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].paymentCode").value(containsString("TIC")));

        doGet("/api/payments/filter", bearer(user))
                .andExpect(status().isForbidden());
    }
}