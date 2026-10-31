package com.movieticketbooking.service.impl;

import com.movieticketbooking.model.RefreshToken;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.RefreshTokenRepository;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.auth.RefreshTokenRequest;
import com.movieticketbooking.response.AuthResponse;
import com.movieticketbooking.service.EmailService;
import com.movieticketbooking.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplRefreshTokenTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("AUTH-REF-01 - Refresh token hợp lệ")
    void AUTH_REF_01_refreshesValidToken() {
        String rawToken = "RT-1";
        User user = user("Nguyen Van A", "user@mail.com", Role.USER, 7);
        RefreshToken storedToken = refreshToken(rawToken, user, false, LocalDateTime.now().plusDays(1));
        RefreshTokenRequest request = request(rawToken);
        when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(storedToken));
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("ACCESS_NEW");

        AuthResponse response = authService.refreshToken(request);

        assertResponse(response, "ACCESS_NEW", rawToken, user);
        verify(jwtService).generateToken(user);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AUTH-REF-02 - Refresh token không tồn tại")
    void AUTH_REF_02_rejectsMissingToken() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(request("unknown")));

        assertEquals("Refresh token not found or invalid", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("AUTH-REF-03 - Refresh token đã bị revoke")
    void AUTH_REF_03_rejectsRevokedToken() {
        RefreshToken storedToken = refreshToken(
                "RT-1", user("Nguyen Van A", "user@mail.com", Role.USER, 7),
                true, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("RT-1")).thenReturn(Optional.of(storedToken));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(request("RT-1")));

        assertEquals("Refresh token has been revoked. Please log in again.", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("AUTH-REF-04 - Refresh token hết hạn")
    void AUTH_REF_04_revokesAndRejectsExpiredToken() {
        RefreshToken storedToken = refreshToken(
                "RT-1", user("Nguyen Van A", "user@mail.com", Role.USER, 7),
                false, LocalDateTime.now().minusHours(1));
        when(refreshTokenRepository.findByToken("RT-1")).thenReturn(Optional.of(storedToken));

        // Unit test với mock không kiểm chứng rollback của @Transactional.
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(request("RT-1")));

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        InOrder order = inOrder(refreshTokenRepository);
        order.verify(refreshTokenRepository).findByToken("RT-1");
        order.verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertTrueRevoked(tokenCaptor.getValue());
        assertEquals("Refresh token has expired. Please log in again.", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("AUTH-REF-05(a) - Hết hạn sau 5 giây")
    void AUTH_REF_05_a_expiresIn5Seconds() {
        String rawToken = "RT-1";
        User user = user("Nguyen Van A", "user@mail.com", Role.USER, 7);
        RefreshToken storedToken = refreshToken(rawToken, user, false, LocalDateTime.now().plusSeconds(5));
        when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(storedToken));
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("ACCESS_NEW");

        AuthResponse response = authService.refreshToken(request(rawToken));

        assertResponse(response, "ACCESS_NEW", rawToken, user);
        verify(jwtService).generateToken(user);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AUTH-REF-05(b) - Đã hết hạn 5 giây")
    void AUTH_REF_05_b_expired5SecondsAgo() {
        RefreshToken storedToken = refreshToken(
                "RT-1", user("Nguyen Van A", "user@mail.com", Role.USER, 7),
                false, LocalDateTime.now().minusSeconds(5));
        when(refreshTokenRepository.findByToken("RT-1")).thenReturn(Optional.of(storedToken));

        // Không kiểm thử expiresAt == now vì refreshToken dùng LocalDateTime.now() trực tiếp,
        // chưa inject Clock nên kết quả phụ thuộc thời điểm chạy.
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(request("RT-1")));

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertTrueRevoked(tokenCaptor.getValue());
        assertEquals("Refresh token has expired. Please log in again.", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
        verifyNoInteractions(userDetailsService);
    }

    private static RefreshTokenRequest request(String rawToken) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);
        return request;
    }

    private static RefreshToken refreshToken(
            String rawToken, User user, boolean revoked, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(rawToken);
        refreshToken.setUser(user);
        refreshToken.setRevoked(revoked);
        refreshToken.setExpiresAt(expiresAt);
        return refreshToken;
    }

    private static User user(String name, String email, Role role, int id) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setId(id);
        return user;
    }

    private static void assertResponse(
            AuthResponse response, String token, String refreshToken, User user) {
        assertEquals(token, response.getToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole().name(), response.getRole());
    }

    private static void assertTrueRevoked(RefreshToken refreshToken) {
        assertFalse(!refreshToken.isRevoked());
    }
}
