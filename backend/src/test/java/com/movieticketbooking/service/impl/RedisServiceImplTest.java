package com.movieticketbooking.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisServiceImpl redisService;

    @Test
    @DisplayName("REDIS-01 - Set value")
    void REDIS_01_setsValueWithoutTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.set("k1", "v1");

        verify(valueOperations).set("k1", "v1");
    }

    @Test
    @DisplayName("REDIS-02 - Set value có TTL")
    void REDIS_02_setsValueWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.setWithExpiration(
                "otp_reset_a@mail.com",
                "123456",
                300,
                TimeUnit.SECONDS);

        verify(valueOperations).set(
                "otp_reset_a@mail.com",
                "123456",
                300,
                TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("REDIS-03 - Get value")
    void REDIS_03_getsValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("k1")).thenReturn("v1");

        Object result = redisService.get("k1");

        assertEquals("v1", result);
        verify(valueOperations).get("k1");
    }

    @Test
    @DisplayName("REDIS-04 - Delete value")
    void REDIS_04_deletesValue() {
        redisService.delete("k1");

        verify(redisTemplate).delete("k1");
    }

    @Test
    @DisplayName("REDIS-05 - Exists và expire")
    void REDIS_05_checksExistenceAndExpiresKey() {
        when(redisTemplate.hasKey("k1")).thenReturn(true);
        when(redisTemplate.expire("k1", 60, TimeUnit.SECONDS)).thenReturn(true);

        boolean exists = redisService.exists("k1");
        boolean expired = redisService.expire("k1", 60, TimeUnit.SECONDS);

        assertTrue(exists);
        assertTrue(expired);
        verify(redisTemplate).hasKey("k1");
        verify(redisTemplate).expire("k1", 60, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("REDIS-06 - Increment với delta âm")
    void REDIS_06_rejectsNegativeDelta() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> redisService.increment("counter", -1));

        assertEquals("Delta must be greater than 0", exception.getMessage());
        verify(redisTemplate, never()).opsForValue();
        verify(valueOperations, never()).increment("counter", -1);
    }

    @Test
    @DisplayName("REDIS-07 - Increment với delta 0 và dương")
    void REDIS_07_incrementsZeroAndPositiveDelta() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("counter", 0)).thenReturn(0L);
        when(valueOperations.increment("counter", 1)).thenReturn(5L);

        // Current Behavior: delta = 0 is accepted although the exception message says greater than 0.
        long zeroResult = redisService.increment("counter", 0);
        long positiveResult = redisService.increment("counter", 1);

        assertEquals(0L, zeroResult);
        assertEquals(5L, positiveResult);
        verify(valueOperations).increment("counter", 0);
        verify(valueOperations).increment("counter", 1);
    }

    @Test
    @DisplayName("REDIS-08 - Increment khi Redis trả null hoặc ném exception")
    void REDIS_08_handlesNullAndPropagatesRedisException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("counter", 1))
                .thenReturn(null)
                .thenThrow(new RuntimeException("Redis down"));

        long nullResult = redisService.increment("counter", 1);

        assertEquals(0L, nullResult);
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> redisService.increment("counter", 1));

        assertEquals("Redis down", exception.getMessage());
        verify(valueOperations, org.mockito.Mockito.times(2))
                .increment("counter", 1);
    }
}
