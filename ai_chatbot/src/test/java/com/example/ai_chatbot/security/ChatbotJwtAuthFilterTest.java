package com.example.ai_chatbot.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatbotJwtAuthFilterTest {

    private static final String SECRET =
            "chatbot-test-secret-that-is-longer-than-thirty-two-bytes";

    @Test
    void rejectsProtectedRequestWithoutToken() throws Exception {
        ChatbotJwtUtil jwtUtil = configuredJwtUtil();
        ChatbotJwtAuthFilter filter = new ChatbotJwtAuthFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/chatbot/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertNull(request.getAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID));
    }

    @Test
    void addsUserIdFromValidToken() throws Exception {
        ChatbotJwtUtil jwtUtil = configuredJwtUtil();
        ChatbotJwtAuthFilter filter = new ChatbotJwtAuthFilter(jwtUtil);
        String token = Jwts.builder()
                .subject("42")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/chatbot/chat");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        assertEquals(42, request.getAttribute(ChatbotJwtAuthFilter.AUTHENTICATED_USER_ID));
    }

    @Test
    void failsClosedWhenSecretIsMissing() {
        ChatbotJwtUtil jwtUtil = new ChatbotJwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "");

        assertNull(jwtUtil.getUserId("any-token"));
    }

    private ChatbotJwtUtil configuredJwtUtil() {
        ChatbotJwtUtil jwtUtil = new ChatbotJwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        return jwtUtil;
    }
}
