package com.codivio.security;

import com.codivio.common.Result;
import com.codivio.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器 - 修复版本
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 公开路径列表，这些路径不需要JWT验证
    // 注意：由于设置了context-path: /api/v1，这里的路径是完整路径
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/v1/auth/",
            "/api/v1/health",
            "/api/v1/debug/",
            "/api/v1/actuator/",
            "/error",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // 检查是否是公开路径
        if (isPublicPath(requestPath)) {
            log.debug("跳过JWT验证，公开路径: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        if (token != null) {
            try {
                // 验证Token
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                if (userId != null && username != null && jwtUtil.isAccessToken(token)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

                    // 设置认证信息到SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT认证成功: userId={}, username={}", userId, username);
                }
            } catch (Exception e) {
                log.warn("JWT认证失败: {}", e.getMessage());
                handleAuthenticationError(response, "Token验证失败");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否是公开路径
     */
    private boolean isPublicPath(String requestPath) {
        return PUBLIC_PATHS.stream().anyMatch(requestPath::startsWith);
    }

    /**
     * 处理认证错误
     */
    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Result<Object> result = Result.unauthorized(message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }
}