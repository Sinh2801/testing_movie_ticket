package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceAlreadyExistException;
import com.movieticketbooking.model.RefreshToken;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.RefreshTokenRepository;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.auth.RegisterRequest;
import com.movieticketbooking.response.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplRegisterTest {

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
    private com.movieticketbooking.service.EmailService emailService;

    @Mock
    private com.movieticketbooking.service.RedisService redisService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("AUTH-REG-01 - Đăng ký hợp lệ")
    void AUTH_REG_01_registersValidUser() {
        RegisterRequest request = request(
                "new@mail.com",
                "Pass@123",
                "Pass@123",
                "Nguyen Van A",
                "0901234567");
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Pass@123")).thenReturn("ENC_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("ACCESS_TOKEN");
        when(jwtService.generateRefreshToken()).thenReturn("REFRESH_TOKEN");
        when(jwtService.getRefreshExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("ENC_HASH", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals("new@mail.com", savedUser.getEmail());
        assertEquals("Nguyen Van A", savedUser.getName());
        assertEquals("0901234567", savedUser.getPhoneNumber());
        assertEquals("ACCESS_TOKEN", response.getToken());
        assertEquals("REFRESH_TOKEN", response.getRefreshToken());
        assertEquals("new@mail.com", response.getEmail());
        assertEquals("Nguyen Van A", response.getName());
        assertEquals("USER", response.getRole());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AUTH-REG-02 - Chặn email trùng")
    void AUTH_REG_02_rejectsExistingEmail() {
        RegisterRequest request = request(
                "exist@mail.com",
                "Pass@123",
                "Pass@123",
                null,
                null);
        User existingUser = new User();
        existingUser.setEmail("exist@mail.com");
        when(userRepository.findByEmail("exist@mail.com")).thenReturn(Optional.of(existingUser));

        ResourceAlreadyExistException exception = assertThrows(
                ResourceAlreadyExistException.class,
                () -> authService.register(request));

        assertEquals("Email already exist", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(jwtService, never()).generateRefreshToken();
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AUTH-REG-03 - Password không khớp")
    void AUTH_REG_03_rejectsMismatchedPasswords() {
        RegisterRequest request = request(
                "new@mail.com",
                "Pass@123",
                "Pass@456",
                null,
                null);
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request));

        assertEquals("Passwords do not match", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(jwtService, never()).generateRefreshToken();
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AUTH-REG-04 - Password null - Current Behavior")
    void AUTH_REG_04_recordsCurrentBehaviorForNullPassword() {
        RegisterRequest request = request(
                "new@mail.com",
                null,
                "Pass@123",
                null,
                null);
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());

        // Current Behavior: register() has no null guard before password.equals(...).
        assertThrows(NullPointerException.class, () -> authService.register(request));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AUTH-REG-05 - Refresh token được lưu đúng thông tin")
    void AUTH_REG_05_savesRefreshTokenDetails() {
        RegisterRequest request = request(
                "new@mail.com",
                "Pass@123",
                "Pass@123",
                "Nguyen Van A",
                "0901234567");
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Pass@123")).thenReturn("ENC_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("ACCESS_TOKEN");
        when(jwtService.generateRefreshToken()).thenReturn("RT-FIXED-001");
        when(jwtService.getRefreshExpirationMs()).thenReturn(604800000L);

        LocalDateTime t0 = LocalDateTime.now();
        AuthResponse response = authService.register(request);
        LocalDateTime t1 = LocalDateTime.now();

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken refreshToken = tokenCaptor.getValue();
        assertEquals("RT-FIXED-001", refreshToken.getToken());
        assertNotNull(refreshToken.getUser());
        assertEquals("new@mail.com", refreshToken.getUser().getEmail());
        assertFalse(refreshToken.isRevoked());
        assertTrue(!refreshToken.getCreatedAt().isBefore(t0));
        assertTrue(!refreshToken.getCreatedAt().isAfter(t1));
        LocalDateTime expectedExpiry = refreshToken.getCreatedAt().plusSeconds(604800L);
        assertTrue(Duration.between(expectedExpiry, refreshToken.getExpiresAt()).abs().compareTo(Duration.ofSeconds(2)) <= 0);
        assertEquals("RT-FIXED-001", response.getRefreshToken());
    }

    private static RegisterRequest request(
            String email,
            String password,
            String confirmPassword,
            String name,
            String phoneNumber) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        request.setName(name);
        request.setPhoneNumber(phoneNumber);
        return request;
    }
}
