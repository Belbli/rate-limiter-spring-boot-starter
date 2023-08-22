package com.github.belbli.requestlimiter.limiter.impl;

import com.github.belbli.requestlimiter.limiter.LimitCheckResult;
import com.github.belbli.requestlimiter.limiter.RateLimiter;
import com.github.belbli.requestlimiter.resolver.KeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;

public class RedisRateLimiter implements RateLimiter {
    private static final String KEY_FORMAT = "rl:%s";
    private static final LimitCheckResult LIMIT_CHECK_RESULT_EMPTY_KEY = new LimitCheckResult(false, "key for limiting api requests is empty.", 403);
    private static final LimitCheckResult LIMIT_CHECK_RESULT_REQUESTS_LIMIT_EXCEEDED = new LimitCheckResult(false, "requests limit exceeded.", 429);
    private static final LimitCheckResult LIMIT_CHECK_RESULT_ALLOW = new LimitCheckResult(true, "", 200);
    private final RedisTemplate<String, Long> redisTemplate;
    private final KeyResolver keyResolver;

    @Value("${rate-limiter.max-rate-limit:10}")
    private Integer maxRateLimit;

    @Value("${rate-limiter.allow-empty-key:false}")
    private Boolean allowEmptyKey;

    public RedisRateLimiter(RedisTemplate<String, Long> redisTemplate, KeyResolver keyResolver) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
    }

    @Override
    public LimitCheckResult checkLimit(HttpServletRequest request) {
        String id = keyResolver.getKey(request);
        if ((id == null || id.isBlank()) && !allowEmptyKey) {
            return LIMIT_CHECK_RESULT_EMPTY_KEY;
        }
        String key = KEY_FORMAT.formatted(id);

        Long requests = redisTemplate.opsForValue().get(key);
        requests = requests == null ? 0 : requests;
        if (requests >= maxRateLimit) return LIMIT_CHECK_RESULT_REQUESTS_LIMIT_EXCEEDED;

        incrementAndExpireKey(key);
        return LIMIT_CHECK_RESULT_ALLOW;
    }

    private void incrementAndExpireKey(String key) {
        redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
            byte[] keyBytes = key.getBytes();
            return List.of(
                    connection.stringCommands().incr(keyBytes),
                    connection.keyCommands().expire(keyBytes, Duration.ofSeconds(59L).getSeconds())
            );
        });
    }

}
