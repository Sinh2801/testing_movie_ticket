package com.movieticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.ShowTime;
import com.movieticketbooking.model.ShowTimeSeat;
import com.movieticketbooking.model.TicketSeat;
import com.movieticketbooking.model.TicketStatus;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.TicketSeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho module Ticket (IT-TICKET-01 .. IT-TICKET-22).
 * Luồng: Controller -> TicketService -> Repository -> MySQL.
 */
class TicketIntegrationTest extends IntegrationTestBase {

    private static final String BOOK = "/api/tickets/book";

    private LocalDateTime future() {
        return LocalDateTime.now().plusDays(3);
    }

    // =====================================================================
    //  Đặt vé (IT-TICKET-01 .. 10)
    // =====================================================================

    @Test
    @DisplayName("IT-TICKET-01: Đặt vé thành công 1 ghế")
    void it01_bookOneSeat() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 3);
        int seatId = seatIdAt(st, 0);

        MvcResult result = doPost(BOOK, bookBody(st, List.of(seatId)), bearer(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking ticket success"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertTrue(data.path("uid").asText().matches("TIC-[0-9A-F]{8}"), "uid phải dạng TIC-XXXXXXXX");
        assertEquals(0, DEFAULT_PRICE.compareTo(new BigDecimal(data.path("totalPrice").asText())),
                "totalPrice = giá suất chiếu x 1");

        assertEquals(1, ticketRepository.count());
        assertEquals(1, ticketSeatRepository.count());
        assertEquals(TicketStatus.PENDING, ticketRepository.findAll().get(0).getStatus());
        assertEquals(SeatStatus.RESERVED, seatStatus(seatId), "ghế chuyển sang RESERVED");
    }

    @Test
    @DisplayName("IT-TICKET-02: Đặt vé nhiều ghế")
    void it02_bookMultipleSeats() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 3);
        List<Integer> seatIds = List.of(seatIdAt(st, 0), seatIdAt(st, 1), seatIdAt(st, 2));

        MvcResult result = doPost(BOOK, bookBody(st, seatIds), bearer(user))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertEquals(0, DEFAULT_PRICE.multiply(BigDecimal.valueOf(3))
                .compareTo(new BigDecimal(data.path("totalPrice").asText())), "totalPrice = giá x 3");
        assertEquals(3, data.path("seatNames").size(), "response có đủ 3 seatNames");

        assertEquals(1, ticketRepository.count(), "1 ticket");
        assertEquals(3, ticketSeatRepository.count(), "3 ticket_seats");
        for (ShowTimeSeat sts : seatsOf(st)) {
            assertEquals(SeatStatus.RESERVED, seatStatus(sts.getId()));
        }
    }

    @Test
    @DisplayName("IT-TICKET-03: Đặt vé khi chưa đăng nhập")
    void it03_bookWithoutToken() throws Exception {
        ShowTime st = createShowTime(future(), 1);

        int httpStatus = doPost(BOOK, bookBody(st, List.of(seatIdAt(st, 0))), null)
                .andReturn().getResponse().getStatus();

        assertTrue(httpStatus == 401 || httpStatus == 403,
                "phải bị chặn với 401 hoặc 403, nhận được: " + httpStatus);
        assertEquals(0, ticketRepository.count(), "DB không tạo ticket");
    }

    @Test
    @DisplayName("IT-TICKET-04: Đặt vé cho suất chiếu không tồn tại")
    void it04_bookUnknownShowtime() throws Exception {
        User user = createUser(Role.USER);
        Map<String, Object> body = Map.of("showtimeId", 999999, "showtimeSeatIds", List.of(1));

        doPost(BOOK, body, bearer(user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("ShowTime not found with id")));
    }

    @Test
    @DisplayName("IT-TICKET-05: Đặt vé cho suất chiếu đã qua")
    void it05_bookPastShowtime() throws Exception {
        User user = createUser(Role.USER);
        ShowTime past = createShowTime(LocalDateTime.now().minusHours(1), 1);

        doPost(BOOK, bookBody(past, List.of(seatIdAt(past, 0))), bearer(user))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("suất chiếu đã qua")));

        assertEquals(0, ticketRepository.count(), "DB không tạo ticket");
    }

    @Test
    @DisplayName("IT-TICKET-06: Đặt vé với ghế không tồn tại")
    void it06_bookUnknownSeat() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);

        doPost(BOOK, bookBody(st, List.of(999999)), bearer(user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ShowTimeSeat not found with id: 999999"));

        assertEquals(0, ticketRepository.count());
    }

    @Test
    @DisplayName("IT-TICKET-07: Đặt ghế đã được người khác đặt")
    void it07_bookSeatAlreadyTaken() throws Exception {
        User userA = createUser(Role.USER);
        User userB = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);

        doPost(BOOK, bookBody(st, List.of(seatId)), bearer(userA)).andExpect(status().isOk());

        doPost(BOOK, bookBody(st, List.of(seatId)), bearer(userB))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("is not available")));

        assertEquals(1, ticketRepository.count(), "không có ticket mới của B");
        assertEquals(1, ticketSeatRepository.count());
    }

    @Test
    @DisplayName("IT-TICKET-08: Đặt nhiều ghế, 1 ghế đã bị đặt (kiểm tra rollback)")
    void it08_bookBatchWithOneTakenSeat() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 2);
        int seatX = seatIdAt(st, 0);
        int seatY = seatIdAt(st, 1);

        // Ghế Y đã được giữ trước đó
        ShowTimeSeat y = showTimeSeatRepository.findById(seatY).orElseThrow();
        y.setStatus(SeatStatus.RESERVED);
        showTimeSeatRepository.save(y);

        doPost(BOOK, bookBody(st, List.of(seatX, seatY)), bearer(user))
                .andExpect(status().isConflict());

        assertEquals(SeatStatus.AVAILABLE, seatStatus(seatX), "ghế X vẫn AVAILABLE (không bị đổi khi lỗi)");
        assertEquals(0, ticketRepository.count(), "không có ticket mới được lưu");
        assertEquals(0, ticketSeatRepository.count());
    }

    @Test
    @DisplayName("IT-TICKET-09: Đặt vé với dữ liệu thiếu")
    void it09_bookMissingData() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);

        // Danh sách ghế rỗng
        doPost(BOOK, Map.of("showtimeId", st.getId(), "showtimeSeatIds", List.of()), bearer(user))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dữ liệu đầu vào không hợp lệ"))
                .andExpect(jsonPath("$.data.showtimeSeatIds").exists());

        // Thiếu showtimeId
        doPost(BOOK, Map.of("showtimeSeatIds", List.of(seatIdAt(st, 0))), bearer(user))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dữ liệu đầu vào không hợp lệ"))
                .andExpect(jsonPath("$.data.showtimeId").exists());

        assertEquals(0, ticketRepository.count());
    }

    @Test
    @DisplayName("IT-TICKET-10: Hai user đặt cùng 1 ghế đồng thời")
    void it10_concurrentBookingSameSeat() throws Exception {
        User userA = createUser(Role.USER);
        User userB = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();
        try {
            for (User user : List.of(userA, userB)) {
                Callable<Integer> task = () -> {
                    startSignal.await();
                    return doPost(BOOK, bookBody(st, List.of(seatId)), bearer(user))
                            .andReturn().getResponse().getStatus();
                };
                futures.add(pool.submit(task));
            }
            startSignal.countDown(); // bắt đầu 2 request cùng lúc

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : futures) {
                statuses.add(f.get(60, TimeUnit.SECONDS));
            }
            Collections.sort(statuses);
            assertEquals(List.of(200, 409), statuses, "đúng 1 request 200 và 1 request 409");
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, ticketRepository.count(), "chỉ 1 vé được tạo");
        long activeSeatLinks = ticketSeatRepository.findAll().stream()
                .filter(ts -> ts.getCancelledAt() == null)
                .count();
        assertEquals(1, activeSeatLinks, "ghế X chỉ thuộc 1 ticket đang hoạt động");
        assertEquals(SeatStatus.RESERVED, seatStatus(seatId));
    }

    // =====================================================================
    //  Xem lịch sử / chi tiết vé (IT-TICKET-11 .. 16)
    // =====================================================================

    @Test
    @DisplayName("IT-TICKET-11: Xem lịch sử vé của user")
    void it11_historyOnlyOwnTickets() throws Exception {
        User userA = createUser(Role.USER);
        User userB = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 3);

        bookViaApi(bearer(userA), st, List.of(seatIdAt(st, 0)));
        bookViaApi(bearer(userA), st, List.of(seatIdAt(st, 1)));
        bookViaApi(bearer(userB), st, List.of(seatIdAt(st, 2)));

        MvcResult result = doGet("/api/tickets/history", bearer(userA))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode list = readJson(result).path("data");
        assertEquals(2, list.size(), "chỉ trả về 2 vé của A");
        for (JsonNode item : list) {
            assertEquals(userA.getEmail(), item.path("userEmail").asText(), "mọi vé phải thuộc A");
        }
        LocalDateTime first = LocalDateTime.parse(list.get(0).path("createdAt").asText());
        LocalDateTime second = LocalDateTime.parse(list.get(1).path("createdAt").asText());
        assertFalse(first.isBefore(second), "sắp xếp theo createdAt giảm dần");
    }

    @Test
    @DisplayName("IT-TICKET-12: Xem chi tiết vé của chính mình")
    void it12_viewOwnTicketById() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        JsonNode booked = bookViaApi(bearer(user), st, List.of(seatIdAt(st, 0)));
        int ticketId = booked.path("id").asInt();

        doGet("/api/tickets/" + ticketId, bearer(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.movieName").value("Test Movie"))
                .andExpect(jsonPath("$.data.cinemaName").value("Test Cinema"))
                .andExpect(jsonPath("$.data.roomName").value("Room 1"))
                .andExpect(jsonPath("$.data.startTime").exists())
                .andExpect(jsonPath("$.data.seatNames[0]").value("A1"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("IT-TICKET-13: Xem chi tiết vé theo uid")
    void it13_viewOwnTicketByUid() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        String uid = bookViaApi(bearer(user), st, List.of(seatIdAt(st, 0))).path("uid").asText();

        doGet("/api/tickets/uid/" + uid, bearer(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uid").value(uid));
    }

    @Test
    @DisplayName("IT-TICKET-14: Xem vé của người khác (USER)")
    void it14_userCannotViewOthersTicket() throws Exception {
        User owner = createUser(Role.USER);
        User other = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int ticketId = bookViaApi(bearer(owner), st, List.of(seatIdAt(st, 0))).path("id").asInt();

        doGet("/api/tickets/" + ticketId, bearer(other))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are not authorized to view this ticket details!"));
    }

    @Test
    @DisplayName("IT-TICKET-15: ADMIN xem vé của user khác")
    void it15_adminCanViewAnyTicket() throws Exception {
        User owner = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(future(), 1);
        JsonNode booked = bookViaApi(bearer(owner), st, List.of(seatIdAt(st, 0)));
        int ticketId = booked.path("id").asInt();

        doGet("/api/tickets/" + ticketId, bearer(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uid").value(booked.path("uid").asText()));
    }

    @Test
    @DisplayName("IT-TICKET-16: Xem vé không tồn tại")
    void it16_viewUnknownTicket() throws Exception {
        User user = createUser(Role.USER);

        doGet("/api/tickets/999999", bearer(user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found with id: 999999"));
    }

    // =====================================================================
    //  Hủy vé (IT-TICKET-17 .. 21)
    // =====================================================================

    @Test
    @DisplayName("IT-TICKET-17: Hủy vé thành công")
    void it17_cancelTicketSuccess() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1); // còn hơn 2 giờ
        int seatId = seatIdAt(st, 0);
        int ticketId = bookViaApi(bearer(user), st, List.of(seatId)).path("id").asInt();

        doPostNoBody("/api/tickets/" + ticketId + "/cancel", bearer(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hủy vé thành công!"));

        assertEquals(TicketStatus.CANCELLED, ticketEntity(ticketId).getStatus());
        for (TicketSeat ts : ticketSeatRepository.findAll()) {
            assertNotNull(ts.getCancelledAt(), "cancelled_at phải có giá trị");
        }
        assertEquals(SeatStatus.AVAILABLE, seatStatus(seatId), "ghế trả về AVAILABLE");
    }

    @Test
    @DisplayName("IT-TICKET-18: Hủy vé sát giờ chiếu (dưới 2 giờ)")
    void it18_cancelWithinTwoHours() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(LocalDateTime.now().plusHours(1), 1); // mở bán nhưng còn < 2 giờ
        int seatId = seatIdAt(st, 0);
        int ticketId = bookViaApi(bearer(user), st, List.of(seatId)).path("id").asInt();

        doPostNoBody("/api/tickets/" + ticketId + "/cancel", bearer(user))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("at least 2 hours before the showtime")));

        assertEquals(TicketStatus.PENDING, ticketEntity(ticketId).getStatus(), "vé giữ nguyên trạng thái");
        assertEquals(SeatStatus.RESERVED, seatStatus(seatId), "ghế không đổi");
    }

    @Test
    @DisplayName("IT-TICKET-19: Hủy vé đã hủy")
    void it19_cancelTwice() throws Exception {
        User user = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int ticketId = bookViaApi(bearer(user), st, List.of(seatIdAt(st, 0))).path("id").asInt();

        doPostNoBody("/api/tickets/" + ticketId + "/cancel", bearer(user)).andExpect(status().isOk());

        doPostNoBody("/api/tickets/" + ticketId + "/cancel", bearer(user))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ticket has already been cancelled!"));
    }

    @Test
    @DisplayName("IT-TICKET-20: Hủy vé của người khác")
    void it20_cancelOthersTicket() throws Exception {
        User owner = createUser(Role.USER);
        User other = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int ticketId = bookViaApi(bearer(owner), st, List.of(seatIdAt(st, 0))).path("id").asInt();

        doPostNoBody("/api/tickets/" + ticketId + "/cancel", bearer(other))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are not authorized to cancel this ticket!"));

        assertEquals(TicketStatus.PENDING, ticketEntity(ticketId).getStatus(), "vé của A không đổi");
    }

    @Test
    @DisplayName("IT-TICKET-21: Đặt lại ghế sau khi vé bị hủy")
    void it21_rebookAfterCancel() throws Exception {
        User userA = createUser(Role.USER);
        User userB = createUser(Role.USER);
        ShowTime st = createShowTime(future(), 1);
        int seatId = seatIdAt(st, 0);
        int ticketA = bookViaApi(bearer(userA), st, List.of(seatId)).path("id").asInt();

        doPostNoBody("/api/tickets/" + ticketA + "/cancel", bearer(userA)).andExpect(status().isOk());

        doPost(BOOK, bookBody(st, List.of(seatId)), bearer(userB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        assertEquals(SeatStatus.RESERVED, seatStatus(seatId), "ghế RESERVED cho B");
        assertEquals(2, ticketRepository.count(), "vé cũ vẫn còn (CANCELLED) và có vé mới");
        assertTrue(ticketSeatRepository.findAll().stream().anyMatch(ts -> ts.getCancelledAt() != null),
                "ticket_seats cũ vẫn giữ cancelled_at");
    }

    // =====================================================================
    //  Lọc vé (IT-TICKET-22)
    // =====================================================================

    @Test
    @DisplayName("IT-TICKET-22: ADMIN lọc danh sách vé / USER bị chặn")
    void it22_filterAdminOnly() throws Exception {
        User user = createUser(Role.USER);
        User admin = createUser(Role.ADMIN);
        ShowTime st = createShowTime(future(), 3);
        for (int i = 0; i < 3; i++) {
            bookViaApi(bearer(user), st, List.of(seatIdAt(st, i)));
        }

        doGet("/api/tickets/filter?page=1&size=2&uid=TIC", bearer(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3));

        doGet("/api/tickets/filter", bearer(user))
                .andExpect(status().isForbidden());
    }
}
