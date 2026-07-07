package com.example.Multi_reward.config;

import com.example.Multi_reward.util.LogContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);
    private static final String ATTR_START = "reqStart";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        LogContext.setTraceId(traceId);
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        log.info("event=REQUEST_IN method={} uri={}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long elapsed = System.currentTimeMillis() - (long) request.getAttribute(ATTR_START);
        log.info("event=REQUEST_OUT status={} uri={} durationMs={}", response.getStatus(), request.getRequestURI(), elapsed);
        LogContext.clear();
    }
}