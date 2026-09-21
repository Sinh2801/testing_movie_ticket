package com.movieticketbooking.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {
    void set(String key, Object value);
    void setWithExpiration(String key, Object value, long timeout, TimeUnit unit);
    Object get(String key);
    void delete(String key);
    boolean exists(String key);
    boolean expire(String key, long timeout, TimeUnit unit);
    long increment(String key, long delta);

}
