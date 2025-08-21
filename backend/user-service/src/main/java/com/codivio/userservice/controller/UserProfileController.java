package com.codivio.userservice.controller;

import com.codivio.userservice.dto.ResultVO;
import com.codivio.userservice.entity.User;
import com.codivio.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息管理控制器
 * 
 * 功能范围：
 * - 用户信息查询
 * - 用户信息更新（待实现）
 * - 用户头像管理（待实现）
 * 
 * 安全说明：
 * - 所有接口都需要JWT认证
 * - 用户只能访问自己的信息
 * - 通过Spring Security保护，无需手动验证token
 * 
 * RESTful设计：
 * - 基础路径：/api/v1/users
 * - 遵循用户资源管理的REST规范
 * 
 * @author Codivio Team
 * @since 2025-08-21
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户信息
     * 
     * 接口说明：
     * - URL: GET /api/v1/users/profile
     * - 需要JWT认证：通过Authorization header传递Bearer token
     * - 返回当前登录用户的详细信息（已过滤敏感信息）
     * 
     * 实现原理：
     * 1. Spring Security的JwtAuthenticationFilter已验证token
     * 2. 从Security上下文获取当前用户ID（无需重复验证token）
     * 3. 调用Service层查询用户详细信息
     * 4. 返回统一格式的响应
     * 
     * 安全保证：
     * - 只能查询当前登录用户的信息
     * - 密码等敏感信息已在Service层过滤
     * 
     * @return 当前用户信息的统一响应格式
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功", 
     *   "data": {
     *     "id": 1,
     *     "username": "testuser",
     *     "email": "test@example.com",
     *     "nickname": "Test User",
     *     "status": 1,
     *     "createdAt": "2025-08-21T10:30:00",
     *     "updatedAt": "2025-08-21T10:30:00"
     *   },
     *   "timestamp": 1640995200000
     * }
     */
    @GetMapping("/profile")
    public ResultVO<User> getCurrentUserProfile() {
        try {
            // 1. 从Spring Security上下文获取认证信息
            // 此时JWT已通过JwtAuthenticationFilter验证，用户已通过认证
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // 2. 获取当前用户ID
            // JwtAuthenticationFilter在验证token时将userId存储在details中
            Long userId = (Long) authentication.getDetails();
            
            // 3. 通过Service层查询用户信息
            // Service层会过滤敏感信息（如密码）
            User user = userService.getUserById(userId);
            
            // 4. 返回成功响应
            return ResultVO.success(user);
            
        } catch (RuntimeException e) {
            // 5. 异常处理：返回友好的错误信息
            // 可能的异常：用户不存在、数据库连接异常等
            return ResultVO.error(e.getMessage());
        }
    }
}
