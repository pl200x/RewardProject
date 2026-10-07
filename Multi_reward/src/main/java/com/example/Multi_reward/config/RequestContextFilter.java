package com.example.Multi_reward.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component("serviceRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestContextFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);
    private final String service;
    private final String instance;
    private final boolean exposeInstance;

    public RequestContextFilter(@Value("${eureka.instance.appname}") String service,
            @Value("${eureka.instance.instance-id}") String instance,
            @Value("${observability.instance-header-enabled:false}") boolean exposeInstance) {
        this.service = service;
        this.instance = instance;
        this.exposeInstance = exposeInstance;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String supplied = request.getHeader("X-Request-ID");
        String requestId = supplied != null && supplied.matches("[A-Za-z0-9_-]{1,64}")
                ? supplied : UUID.randomUUID().toString();
        Map<String, String> previous = MDC.getCopyOfContextMap();
        long started = System.nanoTime();
        try {
            setContext(requestId);
            response.setHeader("X-Request-ID", requestId);
            if (exposeInstance) response.setHeader("X-Service-Instance", instance);
            chain.doFilter(request, response);
        } finally {
            // Existing MVC interceptors may clear their own MDC before returning here.
            setContext(requestId);
            log.info("event=HTTP_REQUEST_COMPLETE service={} instance={} requestId={} method={} path={} status={} durationMs={}",
                    service, instance, requestId, request.getMethod(), request.getRequestURI(),
                    response.getStatus(), (System.nanoTime() - started) / 1_000_000);
            if (previous == null) MDC.clear(); else MDC.setContextMap(previous);
        }
    }

    private void setContext(String requestId) {
        MDC.put("traceId", requestId);
        MDC.put("service", service);
        MDC.put("instance", instance);
    }
}
