package com.github.belbli.requestlimiter.config;

import com.github.belbli.requestlimiter.filter.RequestLimiterFilter;
import com.github.belbli.requestlimiter.limiter.RateLimiter;
import com.github.belbli.requestlimiter.limiter.impl.RedisRateLimiter;
import com.github.belbli.requestlimiter.resolver.KeyResolver;
import com.github.belbli.requestlimiter.resolver.impl.IpAddressKeyResolver;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
public class RequestLimiterFilterAutoConfiguration {

    @Bean
    public RedisScript<Boolean> script() {
        return RedisScript.of(new ClassPathResource("scripts/rateLimiter.lua"), Boolean.class);
    }

    @Bean
    @ConditionalOnMissingBean(KeyResolver.class)
    public KeyResolver keyResolver() {
        return new IpAddressKeyResolver();
    }

    @Bean
    public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();

        StringRedisSerializer stringRedisSerializer = StringRedisSerializer.UTF_8;
        GenericToStringSerializer<Long> longToStringSerializer = new GenericToStringSerializer<>(Long.class);

        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(longToStringSerializer);
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter redisRateLimiter(RedisTemplate<String, Long> redisTemplate, KeyResolver keyResolver, RedisScript<Boolean> script) {
        return new RedisRateLimiter(redisTemplate, keyResolver, script);
    }

    @Bean
    public Filter filter(RateLimiter rateLimiter) {
        return new RequestLimiterFilter(rateLimiter);
    }
}
