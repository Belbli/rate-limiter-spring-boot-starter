package com.github.belbli.requestlimiter.resolver;

import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface KeyResolver {
    String getKey(HttpServletRequest request);
}
