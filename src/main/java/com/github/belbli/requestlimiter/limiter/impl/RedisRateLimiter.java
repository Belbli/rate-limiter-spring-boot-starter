package com.github.belbli.requestlimiter.limiter.impl;

import com.github.belbli.requestlimiter.limiter.LimitCheckResult;
import com.github.belbli.requestlimiter.limiter.RateLimiter;
import com.github.belbli.requestlimiter.resolver.KeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

public class RedisRateLimiter implements RateLimiter {
    private static final String KEY_FORMAT = "rl:%s";
    private static final LimitCheckResult LIMIT_CHECK_RESULT_EMPTY_KEY = new LimitCheckResult(false, "key for limiting api requests is empty.", 403);
    private static final LimitCheckResult LIMIT_CHECK_RESULT_REQUESTS_LIMIT_EXCEEDED = new LimitCheckResult(false, "requests limit exceeded.", 429);
    private static final LimitCheckResult LIMIT_CHECK_RESULT_ALLOW = new LimitCheckResult(true, "", 200);
    private final RedisTemplate<String, Long> redisTemplate;
    private final KeyResolver keyResolver;
    private final RedisScript<Boolean> script;

    @Value("${rate-limiter.max-rate-limit:10}")
    private Integer maxRateLimit;

    @Value("${rate-limiter.allow-empty-key:false}")
    private Boolean allowEmptyKey;

    public RedisRateLimiter(RedisTemplate<String, Long> redisTemplate, KeyResolver keyResolver, RedisScript<Boolean> script) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
        this.script = script;
    }

    @Override
    public LimitCheckResult checkLimit(HttpServletRequest request) {
        String id = keyResolver.getKey(request);
        if ((id == null || id.isBlank()) && !allowEmptyKey) {
            return LIMIT_CHECK_RESULT_EMPTY_KEY;
        }
        String key = KEY_FORMAT.formatted(id);

        return redisTemplate.execute(script, List.of(key), maxRateLimit, 60)
                ? LIMIT_CHECK_RESULT_ALLOW
                : LIMIT_CHECK_RESULT_REQUESTS_LIMIT_EXCEEDED;
    }
}
