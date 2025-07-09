package com.codivio.service;

import com.codivio.dto.auth.*;
import com.codivio.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserInfoDTO register(RegisterRequestDTO request);

    /**
     * 用户登录
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * 刷新Token
     */
    LoginResponseDTO refreshToken(String refreshToken);

    /**
     * 根据ID获取用户信息
     */
    UserInfoDTO getUserById(Long userId);

    /**
     * 更新用户信息
     */
    UserInfoDTO updateUser(Long userId, UpdateUserRequestDTO request);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}