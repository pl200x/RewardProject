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

    public static final String AUTHENTICATED_USER_ID = JwtAuthFilter.class.getName() + ".userId";

    /** 需要登录才能访问的接口(精确匹配 URI)。 */
    private static final Set<String> PROTECTED = Set.of(
            "/music/play",
            "/music/upload",
            "/music/add",
            "/like/add",
            "/like/delete",
            "/comment/add",
            "/user/change-tag",
            "/user/change-avatar",
            "/user/logout"
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 预检请求与非保护路径直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !requiresAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;

        Integer authenticatedUserId = token == null ? null : jwtUtil.getUserId(token);
        if (authenticatedUserId == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或令牌无效,请先登录");
            return;
        }

        // 带 userId 查询参数的写操作必须与 JWT subject 一致,不能代替其他登录用户操作。
        String requestedUserId = request.getParameter("userId");
        if (requestedUserId != null) {
            try {
                if (!authenticatedUserId.equals(Integer.valueOf(requestedUserId))) {
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, "不能操作其他用户的数据");
                    return;
                }
            } catch (NumberFormatException e) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "userId 格式错误");
                return;
            }
        }
        request.setAttribute(AUTHENTICATED_USER_ID, authenticatedUserId);
        filterChain.doFilter(request, response);
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        if (PROTECTED.contains(request.getRequestURI())) {
            return true;
        }
        return "/music/id".equals(request.getRequestURI())
                && "DELETE".equalsIgnoreCase(request.getMethod());
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"success\":false,\"code\":%d,\"errorMessage\":\"%s\"}", status, message));
    }
}
