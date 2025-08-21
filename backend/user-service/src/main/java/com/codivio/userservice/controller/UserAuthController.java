package com.codivio.userservice.controller;

import com.codivio.userservice.dto.LoginResponseDTO;
import com.codivio.userservice.dto.ResultVO;
import com.codivio.userservice.dto.UserLoginDTO;
import com.codivio.userservice.dto.UserRegisterDTO;
import com.codivio.userservice.entity.User;
import com.codivio.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     * 
     * @param registerDTO 注册请求数据
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResultVO<User> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        try {
            User user = userService.register(registerDTO);
            // 清空密码信息，不返回给前端
            user.setPasswordHash(null);
            return ResultVO.success(user);
        } catch (RuntimeException e) {
            return ResultVO.error(e.getMessage());
        }
    }
    
    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return true-可用, false-已存在
     */
    @GetMapping("/check-username/{username}")
    public ResultVO<Boolean> checkUsername(@PathVariable String username) {
        try {
            boolean exists = userService.existsByUsername(username);
            return ResultVO.success(!exists); // true表示可用
        } catch (Exception e) {
            return ResultVO.error("检查用户名失败");
        }
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱地址
     * @return true-可用, false-已存在
     */
    @GetMapping("/check-email/{email}")
    public ResultVO<Boolean> checkEmail(@PathVariable String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            return ResultVO.success(!exists); // true表示可用
        } catch (Exception e) {
            return ResultVO.error("检查邮箱失败");
        }
    }
    
    /**
     * 用户登录
     * 
     * @param loginDTO 登录请求数据
     * @return 登录结果（包含JWT Token和用户信息）
     */
    @PostMapping("/login")
    public ResultVO<LoginResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            LoginResponseDTO loginResponse = userService.login(loginDTO);
            return ResultVO.success(loginResponse);
        } catch (RuntimeException e) {
            return ResultVO.error(e.getMessage());
        }
    }
}