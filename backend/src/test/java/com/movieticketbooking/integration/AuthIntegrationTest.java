package com.movieticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticketbooking.model.RefreshToken;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.RefreshTokenRepository;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.service.EmailService;
import com.movieticketbooking.service.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho module Auth (IT-AUTH-01 .. IT-AUTH-18 trong sheet
 * Integration_Test_Cases_Movie_Ticket_Booking.xlsx).
 *
 * Chạy với: MySQL + Redis local (xem application-test.properties).
 * EmailService được mock để kiểm tra việc gọi gửi OTP mà không gửi mail thật.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    private static final String PASSWORD = "Abc12345";
    private static final String NEW_PASSWORD = "NewPass123";
    private static final String WRONG_CREDENTIALS_MSG = "Email hoặc mật khẩu không chính xác!";
    private static final String INVALID_INPUT_MSG = "Dữ liệu đầu vào không hợp lệ";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RedisService redisService;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private PlatformTransactionManager txManager;

    @MockBean private EmailService emailService;

    // Các email được tạo trong test, dùng để dọn dữ liệu sau mỗi test
    private final List<String> createdEmails = new ArrayList<>();

    // =====================================================================
    //  Đăng ký (IT-AUTH-01 .. 04)
    // =====================================================================

    @Test
    @DisplayName("IT-AUTH-01: Đăng ký tài khoản thành công")
    void it01_registerSuccess() throws Exception {
        String email = newEmail();
        long usersBefore = userRepository.count();

        JsonNode json = body(doRegister(email));

        assertEquals("Register success", json.path("message").asText());
        assertFalse(json.path("data").path("token").asText().isBlank(), "phải có access token");
        String refreshToken = json.path("data").path("refreshToken").asText();
        assertFalse(refreshToken.isBlank(), "phải có refresh token");
        assertEquals("USER", json.path("data").path("role").asText());

        // DB: có 1 user mới, mật khẩu đã được mã hóa
        assertEquals(usersBefore + 1, userRepository.count());
        User user = userRepository.findByEmail(email).orElseThrow();
        assertNotEquals(PASSWORD, user.getPassword(), "mật khẩu không được lưu dạng gốc");
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));

        // DB: có 1 refresh token revoked = false
        assertEquals(Boolean.FALSE, revokedState(refreshToken));
    }

    @Test
    @DisplayName("IT-AUTH-02: Đăng ký với email đã tồn tại")
    void it02_registerDuplicateEmail() throws Exception {
        String email = newEmail();
        doRegister(email);
        long usersBefore = userRepository.count();
        int existingId = userRepository.findByEmail(email).orElseThrow().getId();

        postJson("/api/auth/register", registerPayload(email, PASSWORD, PASSWORD))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exist"));

        assertEquals(usersBefore, userRepository.count(), "không được thêm user mới");
        assertEquals(existingId, userRepository.findByEmail(email).orElseThrow().getId());
    }

    @Test
    @DisplayName("IT-AUTH-03: Đăng ký với confirmPassword không khớp")
    void it03_registerPasswordMismatch() throws Exception {
        String email = newEmail();
        long usersBefore = userRepository.count();

        postJson("/api/auth/register", registerPayload(email, PASSWORD, "Abc12346"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Passwords do not match"));

        assertEquals(usersBefore, userRepository.count());
        assertFalse(userRepository.existsByEmail(email));
    }

    @Test
    @DisplayName("IT-AUTH-04: Đăng ký với dữ liệu sai định dạng")
    void it04_registerInvalidFormat() throws Exception {
        String badEmail = "abc.example.com"; // thiếu @
        long usersBefore = userRepository.count();

        Map<String, String> payload = Map.of(
                "name", "Test User",
                "email", badEmail,
                "phoneNumber", "12345",          // sai định dạng SĐT
                "password", "abcdefg1",          // thiếu chữ hoa
                "confirmPassword", "abcdefg1");

        postJson("/api/auth/register", payload)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(INVALID_INPUT_MSG))
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.phoneNumber").exists())
                .andExpect(jsonPath("$.data.password").exists());

        assertEquals(usersBefore, userRepository.count(), "DB không thêm dữ liệu");
    }

    // =====================================================================
    //  Đăng nhập (IT-AUTH-05 .. 08)
    // =====================================================================

    @Test
    @DisplayName("IT-AUTH-05: Đăng nhập thành công")
    void it05_loginSuccess() throws Exception {
        String email = newEmail();
        String oldRefresh = body(doRegister(email)).path("data").path("refreshToken").asText();

        MvcResult result = postJson("/api/auth/login", Map.of("email", email, "password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login success"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andReturn();

        String newRefresh = body(result).path("data").path("refreshToken").asText();
        assertNotEquals(oldRefresh, newRefresh, "phải cấp refresh token mới");
        assertEquals(Boolean.TRUE, revokedState(oldRefresh), "refresh token cũ phải bị revoke");
        assertEquals(Boolean.FALSE, revokedState(newRefresh), "refresh token mới chưa bị revoke");
    }

    @Test
    @DisplayName("IT-AUTH-06: Đăng nhập sai mật khẩu")
    void it06_loginWrongPassword() throws Exception {
        String email = newEmail();
        String refresh = body(doRegister(email)).path("data").path("refreshToken").asText();
        long tokensBefore = refreshTokenCount();

        MvcResult result = postJson("/api/auth/login", Map.of("email", email, "password", "WrongPass1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(WRONG_CREDENTIALS_MSG))
                .andReturn();

        assertTrue(body(result).path("data").isNull(), "data phải null");
        assertEquals(tokensBefore, refreshTokenCount(), "không được tạo refresh token mới");
        assertEquals(Boolean.FALSE, revokedState(refresh), "refresh token cũ không bị ảnh hưởng");
    }

    @Test
    @DisplayName("IT-AUTH-07: Đăng nhập với email không tồn tại")
    void it07_loginUnknownEmail() throws Exception {
        String unknown = newEmail(); // chưa đăng ký

        MvcResult result = postJson("/api/auth/login", Map.of("email", unknown, "password", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Không được tiết lộ email có tồn tại hay không: thông điệp phải giống trường hợp sai mật khẩu
        assertEquals(WRONG_CREDENTIALS_MSG, body(result).path("message").asText());
    }

    @Test
    @DisplayName("IT-AUTH-08: Đăng nhập thiếu dữ liệu bắt buộc")
    void it08_loginMissingFields() throws Exception {
        postJson("/api/auth/login", Map.of("email", "", "password", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(INVALID_INPUT_MSG))
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists());
    }

    // =====================================================================
    //  Refresh token (IT-AUTH-09 .. 12)
    // =====================================================================

    @Test
    @DisplayName("IT-AUTH-09: Refresh token hợp lệ")
    void it09_refreshValid() throws Exception {
        String email = newEmail();
        String refresh = body(doRegister(email)).path("data").path("refreshToken").asText();

        MvcResult result = postJson("/api/auth/refresh", Map.of("refreshToken", refresh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        assertEquals(refresh, body(result).path("data").path("refreshToken").asText(),
                "refresh token phải giữ nguyên");

        String newAccessToken = body(result).path("data").path("token").asText();
        mvc.perform(get("/api/tickets/history")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT-AUTH-10: Refresh token không tồn tại")
    void it10_refreshUnknownToken() throws Exception {
        postJson("/api/auth/refresh", Map.of("refreshToken", "random-" + UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token not found or invalid"));
    }

    @Test
    @DisplayName("IT-AUTH-11: Refresh token đã bị thu hồi (sau logout)")
    void it11_refreshAfterLogout() throws Exception {
        String email = newEmail();
        String refresh = body(doRegister(email)).path("data").path("refreshToken").asText();

        postJson("/api/auth/logout", Map.of("refreshToken", refresh))
                .andExpect(status().isOk());
        assertEquals(Boolean.TRUE, revokedState(refresh), "DB: revoked = true sau logout");

        postJson("/api/auth/refresh", Map.of("refreshToken", refresh))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("has been revoked")));
    }

    @Test
    @DisplayName("IT-AUTH-12: Refresh token hết hạn")
    void it12_refreshExpired() throws Exception {
        String email = newEmail();
        String refresh = body(doRegister(email)).path("data").path("refreshToken").asText();

        // Giả lập token hết hạn bằng cách sửa trực tiếp DB (như mô tả trong sheet)
        inTxVoid(() -> {
            RefreshToken rt = refreshTokenRepository.findByToken(refresh).orElseThrow();
            rt.setExpiresAt(LocalDateTime.now().minusMinutes(1));
            refreshTokenRepository.save(rt);
        });

        postJson("/api/auth/refresh", Map.of("refreshToken", refresh))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("has expired")));

        // Kỳ vọng theo sheet: token chuyển sang revoked = true.
        // LƯU Ý: đây là test phát hiện lỗi. AuthServiceImpl.refreshToken là @Transactional và
        // ném RuntimeException sau khi setRevoked(true), nên thay đổi bị rollback.
        // Test này dự kiến FAIL cho đến khi sửa (ví dụ: @Transactional(noRollbackFor = RuntimeException.class)).
        assertEquals(Boolean.TRUE, revokedState(refresh),
                "DB: token phải chuyển sang revoked = true (đang bị rollback)");
    }

    // =====================================================================
    //  Quên / đặt lại mật khẩu (IT-AUTH-13 .. 18)
    // =====================================================================

    @Test
    @DisplayName("IT-AUTH-13: Quên mật khẩu - gửi OTP")
    void it13_forgotPasswordSendsOtp() throws Exception {
        String email = newEmail();
        doRegister(email);

        postJson("/api/auth/forgot-password", Map.of("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("5 phút")));

        // Redis: key otp_reset_{email} là chuỗi 6 chữ số, TTL <= 300 giây
        String otp = readOtp(email);
        assertTrue(otp.matches("\\d{6}"), "OTP phải gồm đúng 6 chữ số, nhận được: " + otp);
        Long ttl = redisTemplate.getExpire(otpKey(email), TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 300, "TTL phải trong khoảng (0, 300], thực tế: " + ttl);

        // EmailService (mock) được gọi đúng 1 lần
        verify(emailService, times(1)).sendOtpForgotPasswordEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("IT-AUTH-14: Quên mật khẩu - email không tồn tại")
    void it14_forgotPasswordUnknownEmail() throws Exception {
        String unknown = newEmail();

        postJson("/api/auth/forgot-password", Map.of("email", unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("không tồn tại trong hệ thống")));

        assertFalse(redisService.exists(otpKey(unknown)), "Redis không được tạo key");
        verify(emailService, never()).sendOtpForgotPasswordEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("IT-AUTH-15: Đặt lại mật khẩu với OTP đúng")
    void it15_resetPasswordWithValidOtp() throws Exception {
        String email = newEmail();
        doRegister(email);
        postJson("/api/auth/forgot-password", Map.of("email", email)).andExpect(status().isOk());
        String otp = readOtp(email);

        postJson("/api/auth/reset-password",
                Map.of("email", email, "otp", otp, "newPassword", NEW_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("Đặt lại mật khẩu thành công")));

        assertFalse(redisService.exists(otpKey(email)), "OTP phải bị xóa khỏi Redis");

        postJson("/api/auth/login", Map.of("email", email, "password", NEW_PASSWORD))
                .andExpect(status().isOk());
        postJson("/api/auth/login", Map.of("email", email, "password", PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("IT-AUTH-16: Đặt lại mật khẩu với OTP sai")
    void it16_resetPasswordWrongOtp() throws Exception {
        String email = newEmail();
        doRegister(email);
        postJson("/api/auth/forgot-password", Map.of("email", email)).andExpect(status().isOk());
        String realOtp = readOtp(email);
        String wrongOtp = realOtp.equals("000000") ? "111111" : "000000";

        postJson("/api/auth/reset-password",
                Map.of("email", email, "otp", wrongOtp, "newPassword", NEW_PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("không chính xác")));

        assertEquals(realOtp, readOtp(email), "OTP trong Redis vẫn còn nguyên");
        postJson("/api/auth/login", Map.of("email", email, "password", PASSWORD))
                .andExpect(status().isOk()); // mật khẩu cũ vẫn dùng được
    }

    @Test
    @DisplayName("IT-AUTH-17: Đặt lại mật khẩu khi OTP hết hạn")
    void it17_resetPasswordExpiredOtp() throws Exception {
        String email = newEmail();
        doRegister(email);
        postJson("/api/auth/forgot-password", Map.of("email", email)).andExpect(status().isOk());
        String otp = readOtp(email);

        // Giả lập hết hạn: xóa key OTP khỏi Redis (tương đương TTL đã hết)
        redisService.delete(otpKey(email));

        postJson("/api/auth/reset-password",
                Map.of("email", email, "otp", otp, "newPassword", NEW_PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("hết hạn hoặc không hợp lệ")));

        postJson("/api/auth/login", Map.of("email", email, "password", PASSWORD))
                .andExpect(status().isOk()); // mật khẩu không đổi
    }

    @Test
    @DisplayName("IT-AUTH-18: Dùng lại OTP đã sử dụng")
    void it18_reuseOtp() throws Exception {
        String email = newEmail();
        doRegister(email);
        postJson("/api/auth/forgot-password", Map.of("email", email)).andExpect(status().isOk());
        String otp = readOtp(email);

        postJson("/api/auth/reset-password",
                Map.of("email", email, "otp", otp, "newPassword", NEW_PASSWORD))
                .andExpect(status().isOk());

        postJson("/api/auth/reset-password",
                Map.of("email", email, "otp", otp, "newPassword", "Another123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("hết hạn hoặc không hợp lệ")));
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    private String newEmail() {
        String email = "it-auth-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
        createdEmails.add(email);
        return email;
    }

    private Map<String, String> registerPayload(String email, String password, String confirm) {
        return Map.of(
                "name", "Test User",
                "email", email,
                "phoneNumber", "0912345678",
                "password", password,
                "confirmPassword", confirm);
    }

    private MvcResult doRegister(String email) throws Exception {
        return postJson("/api/auth/register", registerPayload(email, PASSWORD, PASSWORD))
                .andExpect(status().isOk())
                .andReturn();
    }

    private ResultActions postJson(String url, Object body) throws Exception {
        return mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String otpKey(String email) {
        return "otp_reset_" + email;
    }

    private String readOtp(String email) {
        Object otp = redisService.get(otpKey(email));
        assertNotNull(otp, "Redis chưa có OTP cho " + email);
        return otp.toString();
    }

    /** Trạng thái revoked của refresh token, null nếu không tìm thấy. */
    private Boolean revokedState(String refreshToken) {
        return inTx(() -> refreshTokenRepository.findByToken(refreshToken)
                .map(RefreshToken::isRevoked)
                .orElse(null));
    }

    private long refreshTokenCount() {
        return inTx(() -> refreshTokenRepository.count());
    }

    private <T> T inTx(Supplier<T> action) {
        return new TransactionTemplate(txManager).execute(status -> action.get());
    }

    private void inTxVoid(Runnable action) {
        new TransactionTemplate(txManager).executeWithoutResult(status -> action.run());
    }

    /** Xóa dữ liệu test sau mỗi case (refresh token trước, rồi user, rồi OTP). */
    /** Xóa bộ đếm rate limit trước mỗi test để tránh 429 khi chạy liên tiếp. */
    @BeforeEach
    void resetRateLimit() {
        redisService.delete("rate_limit:127.0.0.1");
    }

    @AfterEach
    void cleanUp() {
        inTxVoid(() -> {
            for (String email : createdEmails) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    List<RefreshToken> tokens = refreshTokenRepository.findAll().stream()
                            .filter(rt -> rt.getUser().getId() == user.getId())
                            .toList();
                    refreshTokenRepository.deleteAll(tokens);
                    userRepository.delete(user);
                });
                redisService.delete(otpKey(email));
            }
        });
        createdEmails.clear();
    }
}