package com.example.Music_management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtAuthFilterTest {

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", new StubJwtUtil());
    }

    @Test
    void rejectsWriteForDifferentUserId() throws Exception {
        MockHttpServletRequest request = protectedRequest("/user/change-avatar", "8");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
    }

    @Test
    void acceptsWriteForAuthenticatedUserId() throws Exception {
        MockHttpServletRequest request = protectedRequest("/user/change-avatar", "7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        assertEquals(7, request.getAttribute(JwtAuthFilter.AUTHENTICATED_USER_ID));
    }

    private MockHttpServletRequest protectedRequest(String uri, String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.addHeader("Authorization", "Bearer valid-token");
        request.addParameter("userId", userId);
        return request;
    }

    private static class StubJwtUtil extends JwtUtil {
        @Override
        public Integer getUserId(String token) {
            return "valid-token".equals(token) ? 7 : null;
        }
    }
}
