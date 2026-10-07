package com.example.Multi_reward.config;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RequestContextFilterTest {
    @Test
    void preservesSafeCorrelationAndRestoresThreadContextEvenOnFailure() {
        RequestContextFilter filter = new RequestContextFilter("reward-service", "reward-a", false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/searchall");
        request.addHeader("X-Request-ID", "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MDC.put("previous", "value");
        try {
            assertThrows(IllegalStateException.class, () -> filter.doFilter(request, response, (req, res) -> {
                assertEquals("request-123", MDC.get("traceId"));
                assertEquals("reward-a", MDC.get("instance"));
                throw new IllegalStateException("failure");
            }));
            assertEquals("request-123", response.getHeader("X-Request-ID"));
            assertNull(response.getHeader("X-Service-Instance"));
            assertNull(MDC.get("traceId"));
            assertEquals("value", MDC.get("previous"));
        } finally { MDC.clear(); }
    }

    @Test
    void replacesInvalidUntrustedRequestId() throws Exception {
        RequestContextFilter filter = new RequestContextFilter("reward-service", "reward-a", true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/searchall");
        request.addHeader("X-Request-ID", "untrusted\r\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {});
        assertTrue(response.getHeader("X-Request-ID").matches("[A-Za-z0-9_-]{1,64}"));
        assertEquals("reward-a", response.getHeader("X-Service-Instance"));
    }
}
