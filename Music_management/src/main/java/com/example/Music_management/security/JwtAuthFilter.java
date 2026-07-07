package com.example.Music_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 仅拦截"需要登录"的用户接口,校验请求头 Authorization: Bearer <token>。
 * 其余接口(浏览类、登录、注册等)直接放行,不影响原有逻辑。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** 需要登录才能访问的接口(精确匹配 URI)。 */
    private static final Set<String> PROTECTED = Set.of(
            "/music/play",
            "/music/upload",
            "/like/add",
            "/like/delete",
            "/user/change-tag",
            "/user/logout"
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 预检请求与非保护路径直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !PROTECTED.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;

        if (token == null || !jwtUtil.validate(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"errorMessage\":\"未登录或令牌无效,请先登录\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
