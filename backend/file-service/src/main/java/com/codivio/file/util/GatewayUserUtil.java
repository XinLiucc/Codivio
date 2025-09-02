package com.codivio.file.util;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 网关用户工具类
 * 用于从网关传递的请求头中获取用户信息
 */
@Component
public class GatewayUserUtil {

    /**
     * 用户ID请求头名称
     */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名请求头名称
     */
    private static final String USERNAME_HEADER = "X-Username";

    /**
     * 获取当前请求的用户ID
     * 
     * @return 用户ID，如果没有则抛出异常
     * @throws RuntimeException 当无法获取用户ID时抛出
     */
    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new RuntimeException("无法获取当前用户ID，请检查认证状态");
        }
        
        try {
            return Long.valueOf(userIdHeader);
        } catch (NumberFormatException e) {
            throw new RuntimeException("用户ID格式无效: " + userIdHeader);
        }
    }

    /**
     * 获取当前请求的用户名
     * 
     * @return 用户名，如果没有则返回null
     */
    public String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader(USERNAME_HEADER);
    }

    /**
     * 检查当前请求是否已认证
     * 
     * @return 如果已认证返回true，否则返回false
     */
    public boolean isAuthenticated() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String userIdHeader = request.getHeader(USER_ID_HEADER);
            return userIdHeader != null && !userIdHeader.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前HTTP请求对象
     * 
     * @return HttpServletRequest对象
     * @throws RuntimeException 当无法获取请求对象时抛出
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            throw new RuntimeException("无法获取当前HTTP请求上下文");
        }
        
        return attributes.getRequest();
    }

    /**
     * 打印所有请求头信息（调试用）
     */
    public void printAllHeaders() {
        try {
            HttpServletRequest request = getCurrentRequest();
            System.out.println("=== 请求头信息 ===");
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                System.out.println(headerName + ": " + request.getHeader(headerName));
            });
            System.out.println("================");
        } catch (Exception e) {
            System.err.println("无法打印请求头信息: " + e.getMessage());
        }
    }
}