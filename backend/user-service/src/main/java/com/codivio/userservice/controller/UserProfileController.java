package com.codivio.userservice.controller;

import com.codivio.userservice.dto.ResultVO;
import com.codivio.userservice.dto.UserUpdateDTO;
import com.codivio.userservice.entity.User;
import com.codivio.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    }

    /**
     * 更新当前用户信息
     * 
     * 接口说明：
     * - URL: PUT /api/v1/users/profile
     * - 需要JWT认证：通过Authorization header传递Bearer token
     * - 支持部分更新：只更新请求中提供的字段
     * - 返回更新后的用户信息（已过滤敏感信息）
     * 
     * 支持更新的字段：
     * - email: 邮箱地址（会验证格式和唯一性）
     * - nickname: 用户昵称（1-50字符）
     * - avatarUrl: 头像URL（可为空）
     * 
     * 实现原理：
     * 1. Spring Security已验证JWT token并设置认证上下文
     * 2. 从认证上下文获取当前用户ID
     * 3. 调用Service层进行业务验证和数据更新
     * 4. 返回更新后的用户信息
     * 
     * 安全保证：
     * - 用户只能更新自己的信息
     * - 邮箱唯一性验证
     * - 敏感信息已过滤
     * 
     * @param userUpdateDTO 更新数据（经过Bean Validation验证）
     * @return 更新后的用户信息
     * 
     * 请求示例：
     * {
     *   "email": "newemail@example.com",
     *   "nickname": "New Nickname"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "id": 1,
     *     "username": "testuser",
     *     "email": "newemail@example.com",
     *     "nickname": "New Nickname",
     *     "status": 1,
     *     "updatedAt": "2025-08-21T15:30:00"
     *   },
     *   "timestamp": 1640995200000
     * }
     */
    @PutMapping("/profile")
    public ResultVO<User> updateCurrentUserProfile(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        // 1. 从Spring Security上下文获取认证信息
        // JWT认证过滤器已验证token并设置用户认证状态
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 获取当前用户ID
        // JwtAuthenticationFilter在验证token时将userId存储在details中
        Long userId = (Long) authentication.getDetails();

        // 3. 调用Service层执行更新操作
        // Service层会处理业务验证、邮箱唯一性检查、部分更新逻辑
        User updatedUser = userService.updateUser(userId, userUpdateDTO);

        // 4. 返回更新后的用户信息
        return ResultVO.success(updatedUser);
    }
}
