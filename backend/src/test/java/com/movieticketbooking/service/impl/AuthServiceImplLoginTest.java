package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.RefreshToken;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.RefreshTokenRepository;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.auth.LoginRequest;
import com.movieticketbooking.response.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplLoginTest {

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
    @DisplayName("AUTH-LOGIN-01 - Đăng nhập hợp lệ")
    void AUTH_LOGIN_01_logsInValidUserAndRotatesRefreshToken() {
        LoginRequest request = request("user@mail.com", "Pass@123");
        User user = user("Nguyen Van A", "user@mail.com", Role.USER, 7);

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("ACCESS_NEW");
        when(jwtService.generateRefreshToken()).thenReturn("REFRESH_NEW");
        when(jwtService.getRefreshExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.login(request);

        InOrder order = inOrder(authenticationManager, userRepository,
                refreshTokenRepository, jwtService);
        order.verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        order.verify(userRepository).findByEmail("user@mail.com");
        order.verify(refreshTokenRepository).revokeAllUserTokens(7);
        order.verify(jwtService).generateToken(user);
        order.verify(jwtService).generateRefreshToken();
        order.verify(jwtService).getRefreshExpirationMs();
        order.verify(refreshTokenRepository).save(any(RefreshToken.class));

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();
        assertEquals("REFRESH_NEW", savedToken.getToken());
        assertEquals(user, savedToken.getUser());
        assertFalse(savedToken.isRevoked());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());

        assertEquals("ACCESS_NEW", response.getToken());
        assertEquals("REFRESH_NEW", response.getRefreshToken());
        assertEquals("user@mail.com", response.getEmail());
        assertEquals("Nguyen Van A", response.getName());
        assertEquals("USER", response.getRole());
    }

    @Test
    @DisplayName("AUTH-LOGIN-02 - Sai thông tin xác thực")
    void AUTH_LOGIN_02_propagatesBadCredentialsException() {
        LoginRequest request = request("user@mail.com", "WrongPass");
        BadCredentialsException expected = new BadCredentialsException("Bad credentials");
        doThrow(expected).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        BadCredentialsException actual = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request));

        assertEquals(expected, actual);
        verifyNoInteractions(userRepository, refreshTokenRepository, jwtService);
    }

    @Test
    @DisplayName("AUTH-LOGIN-03 - Xác thực đúng nhưng user không tồn tại")
    void AUTH_LOGIN_03_rejectsMissingUser() {
        LoginRequest request = request("ghost@mail.com", "Pass@123");
        when(userRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(request));

        assertEquals("User not found", exception.getMessage());
        verify(refreshTokenRepository, never()).revokeAllUserTokens(any(Integer.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("AUTH-LOGIN-04 - Role null - Current Behavior")
    void AUTH_LOGIN_04_recordsNullRoleCurrentBehavior() {
        LoginRequest request = request("user@mail.com", "Pass@123");
        User user = user("Nguyen Van A", "user@mail.com", null, 7);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("ACCESS_NEW");
        when(jwtService.generateRefreshToken()).thenReturn("REFRESH_NEW");
        when(jwtService.getRefreshExpirationMs()).thenReturn(604800000L);

        // Current Behavior: login() calls user.getRole().name() without a null guard.
        assertThrows(NullPointerException.class, () -> authService.login(request));
    }

    private static LoginRequest request(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private static User user(String name, String email, Role role, int id) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setId(id);
        return user;
    }
}
