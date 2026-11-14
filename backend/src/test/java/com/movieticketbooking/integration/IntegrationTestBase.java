package com.movieticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.model.Movie;
import com.movieticketbooking.model.MovieStatus;
import com.movieticketbooking.model.Room;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.model.SeatStatus;
import com.movieticketbooking.model.SeatType;
import com.movieticketbooking.model.ShowTime;
import com.movieticketbooking.model.ShowTimeSeat;
import com.movieticketbooking.model.Ticket;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.CinemaRepository;
import com.movieticketbooking.repository.MovieRepository;
import com.movieticketbooking.repository.PaymentRepository;
import com.movieticketbooking.repository.RoomRepository;
import com.movieticketbooking.repository.SeatRepository;
import com.movieticketbooking.repository.ShowTimeRepository;
import com.movieticketbooking.repository.ShowTimeSeatRepository;
import com.movieticketbooking.repository.TicketRepository;
import com.movieticketbooking.repository.TicketSeatRepository;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.service.EmailService;
import com.movieticketbooking.service.RedisService;
import com.movieticketbooking.service.impl.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Lớp nền cho các integration test: dựng dữ liệu (fixture), gọi API qua MockMvc,
 * và reset database trước mỗi test để các test độc lập với nhau.
 *
 * Yêu cầu: MySQL và Redis đang chạy theo application-test.properties.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    protected static final String PASSWORD = "Abc12345";
    protected static final BigDecimal DEFAULT_PRICE = new BigDecimal("100000");

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected DataSource dataSource;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JwtService jwtService;
    @Autowired protected UserRepository userRepository;
    @Autowired protected CinemaRepository cinemaRepository;
    @Autowired protected RoomRepository roomRepository;
    @Autowired protected SeatRepository seatRepository;
    @Autowired protected MovieRepository movieRepository;
    @Autowired protected ShowTimeRepository showTimeRepository;
    @Autowired protected ShowTimeSeatRepository showTimeSeatRepository;
    @Autowired protected TicketRepository ticketRepository;
    @Autowired protected TicketSeatRepository ticketSeatRepository;
    @Autowired protected PaymentRepository paymentRepository;
    @Autowired protected PlatformTransactionManager txManager;
    @Autowired protected RedisService redisService;

    /** Mock email để kiểm tra việc gửi mail mà không gửi thật. */
    @MockBean protected EmailService emailService;

    // =====================================================================
    //  Reset database
    // =====================================================================

    /**
     * Xóa bộ đếm rate limit của IP 127.0.0.1 (MockMvc dùng IP này) trước mỗi test,
     * để 58 test chạy liên tiếp không bị 429 Too Many Requests.
     */
    @BeforeEach
    void resetRateLimit() {
        redisService.delete("rate_limit:127.0.0.1");
    }

    /** Truncate toàn bộ bảng trước mỗi test (database test riêng biệt). */
    @BeforeEach
    void resetDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = conn.getMetaData()
                    .getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
            try (Statement st = conn.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String table : tables) {
                    st.execute("TRUNCATE TABLE `" + table + "`");
                }
                st.execute("SET FOREIGN_KEY_CHECKS=1");
            }
        }
    }

    // =====================================================================
    //  Fixture: user, token
    // =====================================================================

    protected String uniqueEmail() {
        return "it-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }

    protected User createUser(Role role) {
        User user = new User();
        user.setName("Tester " + role.name());
        user.setEmail(uniqueEmail());
        user.setPhoneNumber("0912345678");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        return userRepository.save(user);
    }

    /** Access token hợp lệ cho user (sinh trực tiếp bằng JwtService, không qua API login). */
    protected String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    // =====================================================================
    //  Fixture: phim, rạp, phòng, ghế, suất chiếu
    // =====================================================================

    /**
     * Tạo 1 phim, 1 rạp, 1 phòng, {@code seatCount} ghế (A1, A2, ...) và suất chiếu
     * cùng các ShowTimeSeat ở trạng thái AVAILABLE.
     * Mỗi ShowTimeSeat dùng một Seat riêng vì quan hệ Seat-ShowTimeSeat là OneToOne.
     */
    protected ShowTime createShowTime(LocalDateTime startTime, int seatCount) {
        // Toàn bộ entity được tạo trong MỘT transaction để tránh lỗi "detached entity passed to persist"
        // (ShowTimeSeat có cascade ALL lên Seat và ShowTime).
        return new TransactionTemplate(txManager)
                .execute(status -> buildShowTime(startTime, seatCount));
    }

    private ShowTime buildShowTime(LocalDateTime startTime, int seatCount) {
        Movie movie = new Movie();
        movie.setName("Test Movie");
        movie.setDuration(120);
        movie.setMovieStatus(MovieStatus.NOW_SHOWING);
        movie.setGenres(new ArrayList<>());
        movie.setCreatedAt(LocalDateTime.now());
        movie = movieRepository.save(movie);

        Cinema cinema = new Cinema();
        cinema.setName("Test Cinema");
        cinema.setAddress("Test address");
        cinema = cinemaRepository.save(cinema);

        Room room = new Room();
        room.setName("Room 1");
        room.setNumberOfRows(1);
        room.setNumberOfColumns(seatCount);
        room.setCinema(cinema);
        room = roomRepository.save(room);

        ShowTime showTime = new ShowTime();
        showTime.setMovie(movie);
        showTime.setCinema(cinema);
        showTime.setRoom(room);
        showTime.setPrice(DEFAULT_PRICE);
        showTime.setStartTime(startTime);
        showTime.setCreatedAt(LocalDateTime.now());
        showTime = showTimeRepository.save(showTime);

        for (int i = 1; i <= seatCount; i++) {
            Seat seat = new Seat();
            seat.setSeatName("A" + i);
            seat.setRoom(room);
            seat.setType(SeatType.STANDARD);
            seat = seatRepository.save(seat);

            ShowTimeSeat sts = new ShowTimeSeat();
            sts.setSeat(seat);
            sts.setShowTime(showTime);
            sts.setStatus(SeatStatus.AVAILABLE);
            sts.setCreatedAt(LocalDateTime.now());
            showTimeSeatRepository.save(sts);
        }
        return showTime;
    }

    /** Các ShowTimeSeat của suất chiếu, sắp xếp theo id (A1, A2, ...). */
    protected List<ShowTimeSeat> seatsOf(ShowTime showTime) {
        return showTimeSeatRepository.findByShowTimeId(showTime.getId()).stream()
                .sorted(Comparator.comparingInt(ShowTimeSeat::getId))
                .toList();
    }

    protected int seatIdAt(ShowTime showTime, int index) {
        return seatsOf(showTime).get(index).getId();
    }

    protected SeatStatus seatStatus(int showTimeSeatId) {
        return showTimeSeatRepository.findById(showTimeSeatId).orElseThrow().getStatus();
    }

    protected Ticket ticketEntity(int ticketId) {
        return ticketRepository.findById(ticketId).orElseThrow();
    }

    /** Lùi thời điểm tạo vé để giả lập vé đã quá hạn. */
    protected void backdateTicket(int ticketId, LocalDateTime createdAt) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setCreatedAt(createdAt);
        ticketRepository.save(ticket);
    }

    // =====================================================================
    //  API helpers
    // =====================================================================

    protected Map<String, Object> bookBody(ShowTime showTime, List<Integer> seatIds) {
        return Map.of("showtimeId", showTime.getId(), "showtimeSeatIds", seatIds);
    }

    protected ResultActions doPost(String url, Object body, String authorization) throws Exception {
        var request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        if (authorization != null) {
            request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return mvc.perform(request);
    }

    protected ResultActions doPostNoBody(String url, String authorization) throws Exception {
        var request = post(url);
        if (authorization != null) {
            request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return mvc.perform(request);
    }

    protected ResultActions doGet(String url, String authorization) throws Exception {
        var request = get(url);
        if (authorization != null) {
            request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return mvc.perform(request);
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /** Đặt vé qua API, kiểm tra 200 và trả về phần "data" (TicketResponse). */
    protected JsonNode bookViaApi(String authorization, ShowTime showTime, List<Integer> seatIds) throws Exception {
        MvcResult result = doPost("/api/tickets/book", bookBody(showTime, seatIds), authorization)
                .andReturn();
        if (result.getResponse().getStatus() != 200) {
            throw new AssertionError("Đặt vé trong fixture thất bại: HTTP "
                    + result.getResponse().getStatus() + " " + result.getResponse().getContentAsString());
        }
        return readJson(result).path("data");
    }
}