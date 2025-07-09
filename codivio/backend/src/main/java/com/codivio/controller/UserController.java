package com.codivio.controller;

import com.codivio.common.Result;
import com.codivio.dto.auth.UpdateUserRequestDTO;
import com.codivio.dto.auth.UserInfoDTO;
import com.codivio.service.UserService;
import com.codivio.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoDTO> getCurrentUser() {
        Long userId = securityUtil.getCurrentUserId();
        log.info("获取当前用户信息: userId={}", userId);

        UserInfoDTO userInfo = userService.getUserById(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/me")
    public Result<UserInfoDTO> updateCurrentUser(@Valid @RequestBody UpdateUserRequestDTO request) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("更新用户信息: userId={}", userId);

        UserInfoDTO userInfo = userService.updateUser(userId, request);
        return Result.success("更新成功", userInfo);
    }

    /**
     * 根据ID获取用户信息（公开信息）
     */
    @GetMapping("/{userId}")
    public Result<UserInfoDTO> getUserById(@PathVariable Long userId) {
        log.info("获取用户信息: userId={}", userId);

        UserInfoDTO userInfo = userService.getUserById(userId);
        // 移除敏感信息
        userInfo.setEmail(null);

        return Result.success(userInfo);
    }
}