package com.codivio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.codivio.dto.auth.*;
import com.codivio.entity.User;
import com.codivio.exception.BusinessException;
import com.codivio.mapper.UserMapper;
import com.codivio.service.UserService;
import com.codivio.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    @Transactional
    public UserInfoDTO register(RegisterRequestDTO request) {
        // 检查用户名是否已存在
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName() != null ?
                request.getDisplayName() : request.getUsername());

        // 生成盐值和密码哈希
        String salt = generateSalt();
        String passwordHash = passwordEncoder.encode(request.getPassword() + salt);
        user.setSalt(salt);
        user.setPasswordHash(passwordHash);

        // 设置默认值
        user.setStatus(User.Status.NORMAL);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        userMapper.insert(user);

        log.info("用户注册成功: username={}, email={}", request.getUsername(), request.getEmail());

        return convertToUserInfoDTO(user);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 查找用户 - 使用自定义的findByUsername方法
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == User.Status.DISABLED) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword() + user.getSalt(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 构建响应
        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(86400L); // 24小时
        response.setUser(convertToUserInfoDTO(user));

        log.info("用户登录成功: username={}", request.getUsername());

        return response;
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        try {
            // 验证刷新Token
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);

            if (userId == null || username == null) {
                throw new BusinessException("刷新Token无效");
            }

            // 查找用户
            User user = userMapper.selectById(userId);
            if (user == null || !user.getUsername().equals(username)) {
                throw new BusinessException("用户不存在");
            }

            // 生成新的Token
            String newAccessToken = jwtUtil.generateAccessToken(userId, username);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

            LoginResponseDTO response = new LoginResponseDTO();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(86400L);
            response.setUser(convertToUserInfoDTO(user));

            return response;

        } catch (Exception e) {
            throw new BusinessException("刷新Token失败: " + e.getMessage());
        }
    }

    @Override
    public UserInfoDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserInfoDTO(user);
    }

    @Override
    @Transactional
    public UserInfoDTO updateUser(Long userId, UpdateUserRequestDTO request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);

        log.info("用户信息更新成功: userId={}", userId);

        return convertToUserInfoDTO(user);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    /**
     * 生成随机盐值
     */
    private String generateSalt() {
        StringBuilder salt = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            salt.append(SALT_CHARS.charAt(RANDOM.nextInt(SALT_CHARS.length())));
        }
        return salt.toString();
    }

    /**
     * 转换为用户信息DTO
     */
    private UserInfoDTO convertToUserInfoDTO(User user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setCreatedAt(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return dto;
    }
}