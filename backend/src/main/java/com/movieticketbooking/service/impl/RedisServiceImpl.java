package com.movieticketbooking.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.movieticketbooking.service.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // set value with key and value
    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setWithExpiration(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // get value by key
    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // delete value by key
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // check if key exists
    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    // set expiration time for key
    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    // increment value by key and delta
    @SuppressWarnings("null")
    @Override
    public long increment(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("Delta must be greater than 0");
        }
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0L;
    }
    
    
}
