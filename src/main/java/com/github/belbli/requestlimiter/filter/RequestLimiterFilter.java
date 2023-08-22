package com.github.belbli.requestlimiter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.belbli.requestlimiter.limiter.LimitCheckResult;
import com.github.belbli.requestlimiter.limiter.RateLimiter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestLimiterFilter implements Filter {
    @Autowired
    private final RateLimiter rateLimiter;
    @Autowired
    private ObjectMapper mapper;

    public RequestLimiterFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        LimitCheckResult checkResult = rateLimiter.checkLimit(request);
        if (checkResult.isAllowed()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", checkResult.reason());
            httpServletResponse.setStatus(checkResult.httpStatus());
            httpServletResponse.setContentType("application/json");

            mapper.writeValue(httpServletResponse.getWriter(), errorDetails);
        }
    }
}
