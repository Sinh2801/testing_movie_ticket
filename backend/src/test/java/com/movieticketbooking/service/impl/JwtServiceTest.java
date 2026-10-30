package com.movieticketbooking.service.impl;

import com.movieticketbooking.model.Role;
import com.movieticketbooking.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET_BASE64 =
            "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRGb3JUZXN0aW5nMTIzNDU2Nzg5MA==";
    private static final String OTHER_SECRET_BASE64 =
            "QW5vdGhlclN1ZmZpY2llbnRseUxvbmdTZWNyZXRGb3JUZXN0aW5nMTIzNDU2Nzg5MA==";
    private static final long JWT_EXPIRATION_MS = 3_600_000L;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_BASE64);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION_MS);

        user = new User();
        user.setEmail("user@mail.com");
        user.setRole(Role.USER);
    }

    @Test
    @DisplayName("JWT-01 - Tạo và parse access token")
    void JWT_01_generatesAndParsesAccessToken() {
        long before = System.currentTimeMillis();

        String token = jwtService.generateToken(user);

        long after = System.currentTimeMillis();
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.", -1).length);
        assertEquals("user@mail.com", jwtService.extractUsername(token));

        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(issuedAt.getTime() >= before - 1_000);
        assertTrue(issuedAt.getTime() <= after + 1_000);
        assertEquals(JWT_EXPIRATION_MS, expiration.getTime() - issuedAt.getTime(), 1_000);
    }

    @Test
    @DisplayName("JWT-02 - Tạo token có extra claims")
    void JWT_02_generatesTokenWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("uid", 42);

        String token = jwtService.generateToken(extraClaims, user);

        assertEquals("USER", jwtService.extractClaim(token, claims -> claims.get("role", String.class)));
        assertEquals(Integer.valueOf(42), jwtService.extractClaim(token, claims -> claims.get("uid", Integer.class)));
        assertEquals("user@mail.com", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("JWT-03 - Username trùng, token chưa hết hạn")
    void JWT_03_acceptsMatchingUsernameAndUnexpiredToken() {
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    @DisplayName("JWT-04 - Username khác")
    void JWT_04_rejectsDifferentUsername() {
        String token = jwtService.generateToken(user);
        User otherUser = new User();
        otherUser.setEmail("b@mail.com");
        otherUser.setRole(Role.USER);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    @DisplayName("JWT-05 - Token hết hạn")
    void JWT_05_throwsExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String expiredToken = jwtService.generateToken(user);

        ExpiredJwtException exception = assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.extractUsername(expiredToken));
        assertNotNull(exception.getClaims());
        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(expiredToken, user));
    }

    @Test
    @DisplayName("JWT-06 - Sai chữ ký")
    void JWT_06_throwsSignatureExceptionForWrongSecret() {
        JwtService otherJwtService = new JwtService();
        ReflectionTestUtils.setField(otherJwtService, "secretKey", OTHER_SECRET_BASE64);
        ReflectionTestUtils.setField(otherJwtService, "jwtExpiration", JWT_EXPIRATION_MS);
        String token = otherJwtService.generateToken(user);

        assertThrows(SignatureException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("JWT-07 - Token malformed")
    void JWT_07_throwsMalformedJwtException() {
        assertThrows(
                MalformedJwtException.class,
                () -> jwtService.extractUsername("this-is-not-a-jwt"));
    }

    @Test
    @DisplayName("JWT-08 - Secret không phải Base64")
    void JWT_08_throwsDecodingExceptionForInvalidBase64Secret() {
        ReflectionTestUtils.setField(jwtService, "secretKey", "!!!not_base64@@@");

        assertThrows(
                DecodingException.class,
                () -> jwtService.generateToken(user));
        assertThrows(
                DecodingException.class,
                () -> jwtService.extractUsername("this-is-not-a-jwt"));
    }

    @Test
    @DisplayName("JWT-09 - Định dạng refresh token")
    void JWT_09_generatesTwoUuidRefreshToken() {
        String refreshToken1 = jwtService.generateRefreshToken();
        String refreshToken2 = jwtService.generateRefreshToken();

        String[] parts = refreshToken1.split("-", -1);
        assertEquals(10, parts.length);
        assertEquals(
                refreshToken1,
                UUID.fromString(refreshToken1.substring(0, 36))
                        + "-" + UUID.fromString(refreshToken1.substring(37)).toString());
        assertNotEquals(refreshToken1, refreshToken2);
        assertFalse(refreshToken1.split("\\.", -1).length == 3);
    }

    @Test
    @DisplayName("JWT-10 - Getter thời gian hết hạn refresh token")
    void JWT_10_returnsRefreshExpirationInMilliseconds() {
        assertEquals(REFRESH_EXPIRATION_MS, jwtService.getRefreshExpirationMs());
    }
}
