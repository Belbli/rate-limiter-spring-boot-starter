package com.github.belbli.requestlimiter.limiter;

public record LimitCheckResult(
        boolean isAllowed,
        String reason,
        Integer httpStatus
) {
}
