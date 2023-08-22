package com.github.belbli.requestlimiter.limiter;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimiter {
    LimitCheckResult checkLimit(HttpServletRequest request);
}
