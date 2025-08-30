package com.codivio.project.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 网关用户信息工具类
 * 从网关传递的请求头中提取用户信息
 */
@Component
public class GatewayUserUtil {
    
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USERNAME_HEADER = "X-Username";
    
    /**
     * 从网关请求头中获取当前用户ID
     */
    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        String userIdStr = request.getHeader(USER_ID_HEADER);
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            System.err.println("网关传递的用户ID格式无效: " + userIdStr);
            return null;
        }
    }
    
    /**
     * 从网关请求头中获取当前用户名
     */
    public String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        return request.getHeader(USERNAME_HEADER);
    }
    
    /**
     * 获取当前HTTP请求对象
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}