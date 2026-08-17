package com.example.ai_chatbot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Requires a valid user token for every chatbot endpoint. */
@Component
public class ChatbotJwtAuthFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER_ID =
            "com.example.ai_chatbot.security.ChatbotJwtAuthFilter.userId";

    private final ChatbotJwtUtil jwtUtil;

    public ChatbotJwtAuthFilter(ChatbotJwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith("/chatbot/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ")
                ? header.substring(7).trim() : null;
        Integer userId = jwtUtil.getUserId(token);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"code\":401,\"errorMessage\":\"Not logged in or token invalid\"}");
            return;
        }

        request.setAttribute(AUTHENTICATED_USER_ID, userId);
        filterChain.doFilter(request, response);
    }
}
