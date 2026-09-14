package com.movieticketbooking.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.service.RedisService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
public class RateLimitingFilter extends OncePerRequestFilter{
    @Value("${rate.limit.requests:5}")
    private int LIMIT; // default limit for requests per minute
    @Value("${rate.limit.window:60}")
    private int WINDOW_SECONDS; // time window in seconds

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(RedisService redisService, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // skip rate limiting for swagger docs
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        String key = "rate_limit:" + clientIp;

        Long requestCount = redisService.increment(key, 1);
        if (requestCount != null && requestCount == 1) {
            redisService.expire(key, WINDOW_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        }
        if (requestCount != null && requestCount > LIMIT) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ApiResponse apiResponse = new ApiResponse("Too Many Requests. Please try again later.", null);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return;
        }
        filterChain.doFilter(request, response);
    }

}
