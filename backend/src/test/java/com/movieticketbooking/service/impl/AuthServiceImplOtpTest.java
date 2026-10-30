package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.RefreshToken;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.RefreshTokenRepository;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.auth.ForgotPasswordRequest;
import com.movieticketbooking.request.auth.ResetPasswordRequest;
import com.movieticketbooking.service.EmailService;
import com.movieticketbooking.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplOtpTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("AUTH-OTP-01 - Gửi OTP thành công")
    void AUTH_OTP_01_sendsOtpToRedisAndEmail() {
        String email = "user@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user(email)));

        authService.forgotPassword(forgotPasswordRequest(email));

        ArgumentCaptor<String> redisKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> redisOtpCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
        verify(redisService).setWithExpiration(
                redisKeyCaptor.capture(),
                redisOtpCaptor.capture(),
                timeoutCaptor.capture(),
                unitCaptor.capture());

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailOtpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtpForgotPasswordEmail(
                emailCaptor.capture(), emailOtpCaptor.capture());

        assertEquals("otp_reset_user@mail.com", redisKeyCaptor.getValue());
        assertOtp(redisOtpCaptor.getValue());
        assertEquals(300L, timeoutCaptor.getValue());
        assertEquals(TimeUnit.SECONDS, unitCaptor.getValue());
        assertEquals(email, emailCaptor.getValue());
        assertEquals(redisOtpCaptor.getValue(), emailOtpCaptor.getValue());
    }

    @Test
    @DisplayName("AUTH-OTP-02 - Email không tồn tại khi gửi OTP")
    void AUTH_OTP_02_rejectsMissingEmailForForgotPassword() {
        String email = "ghost@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.forgotPassword(forgotPasswordRequest(email)));

        assertEquals(
                "Tài khoản với Email [ghost@mail.com] không tồn tại trong hệ thống!",
                exception.getMessage());
        verifyNoInteractions(redisService, emailService);
    }

    @Test
    @DisplayName("AUTH-OTP-03 - OTP hợp lệ qua 1000 lần sinh")
    void AUTH_OTP_03_generatesValidOtpPropertiesRepeatedly() {
        String email = "user@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user(email)));

        for (int i = 0; i < 1_000; i++) {
            authService.forgotPassword(forgotPasswordRequest(email));
        }

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
        verify(redisService, org.mockito.Mockito.times(1_000)).setWithExpiration(
                org.mockito.ArgumentMatchers.eq("otp_reset_user@mail.com"),
                otpCaptor.capture(),
                timeoutCaptor.capture(),
                unitCaptor.capture());

        assertEquals(1_000, otpCaptor.getAllValues().size());
        for (String otp : otpCaptor.getAllValues()) {
            assertOtp(otp);
        }
        assertTrue(timeoutCaptor.getAllValues().stream().allMatch(timeout -> timeout == 300L));
        assertTrue(unitCaptor.getAllValues().stream().allMatch(unit -> unit == TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("AUTH-OTP-04 - Reset password với OTP hợp lệ")
    void AUTH_OTP_04_resetsPasswordAndDeletesOtp() {
        String email = "user@mail.com";
        User user = user(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(redisService.get("otp_reset_user@mail.com")).thenReturn("123456");
        when(passwordEncoder.encode("NewPass@1")).thenReturn("ENC_NEW");

        authService.resetPassword(resetPasswordRequest(email, "123456", "NewPass@1"));

        assertEquals("ENC_NEW", user.getPassword());
        verify(userRepository).save(user);
        verify(redisService).delete("otp_reset_user@mail.com");
    }

    @Test
    @DisplayName("AUTH-OTP-05 - OTP hết hạn hoặc không có trong Redis")
    void AUTH_OTP_05_rejectsMissingOtp() {
        String email = "user@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user(email)));
        when(redisService.get("otp_reset_user@mail.com")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.resetPassword(
                        resetPasswordRequest(email, "123456", "NewPass@1")));

        assertEquals(
                "Mã OTP đã hết hạn hoặc không hợp lệ. Vui lòng yêu cầu gửi lại OTP!",
                exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(redisService, never()).delete(any(String.class));
    }

    @Test
    @DisplayName("AUTH-OTP-06 - OTP sai")
    void AUTH_OTP_06_rejectsIncorrectOtp() {
        String email = "user@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user(email)));
        when(redisService.get("otp_reset_user@mail.com")).thenReturn("123456");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.resetPassword(
                        resetPasswordRequest(email, "654321", "NewPass@1")));

        assertEquals(
                "Mã OTP 6 chữ số không chính xác. Vui lòng kiểm tra lại!",
                exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(redisService, never()).delete(any(String.class));
    }

    @Test
    @DisplayName("AUTH-OTP-07 - Email reset không tồn tại")
    void AUTH_OTP_07_rejectsMissingResetEmail() {
        String email = "ghost@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.resetPassword(
                        resetPasswordRequest(email, "123456", "NewPass@1")));

        assertEquals(
                "Tài khoản với Email [ghost@mail.com] không tồn tại!",
                exception.getMessage());
        verify(redisService, never()).get(any(String.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(redisService, never()).delete(any(String.class));
    }

    private static ForgotPasswordRequest forgotPasswordRequest(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);
        return request;
    }

    private static ResetPasswordRequest resetPasswordRequest(
            String email, String otp, String newPassword) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email);
        request.setOtp(otp);
        request.setNewPassword(newPassword);
        return request;
    }

    private static User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setRole(Role.USER);
        return user;
    }

    private static void assertOtp(String otp) {
        assertNotNull(otp);
        assertTrue(otp.matches("^\\d{6}$"));
        int numericOtp = Integer.parseInt(otp);
        assertTrue(numericOtp >= 100000 && numericOtp <= 999999);
    }
}
