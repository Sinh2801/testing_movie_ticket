package com.movieticketbooking.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test bảo mật (IT-SEC-01).
 *
 * Class này khai báo rate.limit.requests=50 (đúng cấu hình của sheet), nên Spring tạo một
 * context riêng. Các test khác dùng limit 100000 để không bị 429 khi chạy nhiều request.
 */
@SpringBootTest(properties = {
        "rate.limit.requests=50",
        "rate.limit.window=60"
})
class SecurityIntegrationTest extends IntegrationTestBase {

    private static final String RATE_KEY = "rate_limit:127.0.0.1"; // MockMvc dùng 127.0.0.1 làm IP
    private static final String PROBE_URL = "/api/genres/all";     // endpoint public, nhẹ

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("IT-SEC-01: Giới hạn tần suất request (rate limiting)")
    void it01_rateLimiting() throws Exception {
        // Tiền điều kiện: Redis sạch cho IP này
        redisTemplate.delete(RATE_KEY);

        // 1. 50 request đầu tiên trong 60 giây đều được xử lý
        for (int i = 1; i <= 50; i++) {
            int httpStatus = mvc.perform(get(PROBE_URL)).andReturn().getResponse().getStatus();
            assertNotEquals(429, httpStatus, "request thứ " + i + " không được bị chặn");
        }

        // Request thứ 51 bị từ chối
        mvc.perform(get(PROBE_URL))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.message").value("Too Many Requests. Please try again later."));

        // 2. Kiểm tra Redis key rate_limit:{ip} có TTL xấp xỉ 60 giây
        Long ttl = redisTemplate.getExpire(RATE_KEY, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 60, "TTL phải trong (0, 60], thực tế: " + ttl);

        // 3. Chờ hết cửa sổ rồi gửi lại: request được xử lý bình thường
        Thread.sleep((ttl + 1) * 1000L);
        MvcResult afterWindow = mvc.perform(get(PROBE_URL)).andReturn();
        assertEquals(200, afterWindow.getResponse().getStatus(), "sau khi hết cửa sổ phải được xử lý bình thường");
    }
}
