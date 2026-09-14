package com.movieticketbooking.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {
    private final int TTL_MINUTES = 10;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) 
    {

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(TTL_MINUTES))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Hỗ trợ LocalDateTime, LocalDate
        
        // Lưu thông tin class vào JSON để khi đọc ra Spring biết ép về Class nào
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(
                    @org.springframework.lang.NonNull RuntimeException exception, 
                    @org.springframework.lang.NonNull Cache cache, 
                    @org.springframework.lang.NonNull Object key) {
                log.warn("Redis GET error on cache [{}]: {}. Bypassing Redis cache and reading from Database.", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(
                    @org.springframework.lang.NonNull RuntimeException exception, 
                    @org.springframework.lang.NonNull Cache cache, 
                    @org.springframework.lang.NonNull Object key, 
                    @org.springframework.lang.Nullable Object value) {
                log.warn("Redis PUT error on cache [{}]: {}. Continuing without writing to Redis.", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(
                    @org.springframework.lang.NonNull RuntimeException exception, 
                    @org.springframework.lang.NonNull Cache cache, 
                    @org.springframework.lang.NonNull Object key) {
                log.warn("Redis EVICT error on cache [{}]: {}. Continuing.", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(
                    @org.springframework.lang.NonNull RuntimeException exception, 
                    @org.springframework.lang.NonNull Cache cache) {
                log.warn("Redis CLEAR error on cache [{}]: {}. Continuing.", cache.getName(), exception.getMessage());
            }
        };
    }
}
