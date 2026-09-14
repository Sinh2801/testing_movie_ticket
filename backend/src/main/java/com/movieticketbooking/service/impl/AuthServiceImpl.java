package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceAlreadyExistException;
import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.RefreshToken;
import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.RefreshTokenRepository;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.request.auth.LoginRequest;
import com.movieticketbooking.request.auth.RefreshTokenRequest;
import com.movieticketbooking.request.auth.RegisterRequest;
import com.movieticketbooking.response.AuthResponse;
import com.movieticketbooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // Register account
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistException("Email already exist");
        }
        User user = new User();
        user.setEmail(email);
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        User saved = userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        String rawRefreshToken = createAndSaveRefreshToken(saved);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(jwtToken);
        authResponse.setRefreshToken(rawRefreshToken);
        authResponse.setName(saved.getName());
        authResponse.setEmail(saved.getEmail());
        authResponse.setRole(saved.getRole().name());
        return authResponse;
    }

    // Login account
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Revoke tất cả refresh token cũ của user này trước khi tạo mới
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        String jwtToken = jwtService.generateToken(user);
        String rawRefreshToken = createAndSaveRefreshToken(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(jwtToken);
        authResponse.setRefreshToken(rawRefreshToken);
        authResponse.setName(user.getName());
        authResponse.setEmail(user.getEmail());
        authResponse.setRole(user.getRole().name());
        return authResponse;
    }

    // Refresh access token bằng Refresh Token
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found or invalid"));

        if (storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked. Please log in again.");
        }
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtService.generateToken(
                userDetailsService.loadUserByUsername(user.getEmail()));

        log.info("Access token refreshed for user: {}", user.getEmail());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(newAccessToken);
        authResponse.setRefreshToken(request.getRefreshToken()); // giữ nguyên refresh token
        authResponse.setName(user.getName());
        authResponse.setEmail(user.getEmail());
        authResponse.setRole(user.getRole().name());
        return authResponse;
    }

    // Logout — revoke refresh token
    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Refresh token revoked for user ID: {}", rt.getUser().getId());
        });
    }

    // Create and save a new refresh token for the user
    private String createAndSaveRefreshToken(User user) {
        String rawToken = jwtService.generateRefreshToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(rawToken);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(
                jwtService.getRefreshExpirationMs() / 1000));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }
}
