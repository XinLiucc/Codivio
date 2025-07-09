package com.codivio.controller;

import com.codivio.common.Result;
import com.codivio.dto.auth.*;
import com.codivio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserInfoDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("用户注册请求: username={}, email={}", request.getUsername(), request.getEmail());

        UserInfoDTO userInfo = userService.register(request);
        return Result.success("注册成功", userInfo);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("用户登录请求: username={}", request.getUsername());

        LoginResponseDTO response = userService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        log.info("刷新Token请求");

        LoginResponseDTO response = userService.refreshToken(request.getRefreshToken());
        return Result.success("Token刷新成功", response);
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean available = !userService.existsByUsername(username);
        return Result.success(available);
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean available = !userService.existsByEmail(email);
        return Result.success(available);
    }
}