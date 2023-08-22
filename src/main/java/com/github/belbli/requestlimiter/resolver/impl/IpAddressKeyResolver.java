package com.github.belbli.requestlimiter.resolver.impl;

import com.github.belbli.requestlimiter.resolver.KeyResolver;
import jakarta.servlet.http.HttpServletRequest;

public class IpAddressKeyResolver implements KeyResolver {
    @Override
    public String getKey(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
