package com.codivio.controller;

import com.codivio.common.Result;
import com.codivio.entity.User;
import com.codivio.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调试控制器 - 用于排查问题
 */
@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private com.codivio.service.UserService userService;

    /**
     * 调试getUserById方法
     */
    @GetMapping("/test-get-user-by-id")
    public Result<Map<String, Object>> testGetUserById(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("input", Map.of("userId", userId));

            // 步骤1：直接用MyBatis Plus查询
            User user1 = userMapper.selectById(userId);
            result.put("step1", user1 != null ? "✅ MyBatis Plus查询成功" : "❌ MyBatis Plus查询失败");
            if (user1 != null) {
                result.put("user1", Map.of(
                        "id", user1.getId(),
                        "username", user1.getUsername()
                ));
            }

            // 步骤2：调用Service方法
            try {
                com.codivio.dto.auth.UserInfoDTO userInfo = userService.getUserById(userId);
                result.put("step2", "✅ Service方法调用成功");
                result.put("userInfo", userInfo);
            } catch (Exception e) {
                result.put("step2", "❌ Service方法调用失败: " + e.getMessage());
                result.put("serviceError", e.getClass().getSimpleName());
            }

        } catch (Exception e) {
            log.error("getUserById调试失败", e);
            result.put("error", e.getMessage());
            return Result.error("getUserById调试失败: " + e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 调试登录流程
     */
    @PostMapping("/test-login")
    public Result<Map<String, Object>> testLogin(@org.springframework.web.bind.annotation.RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String username = request.get("username");
        String password = request.get("password");

        try {
            result.put("input", Map.of("username", username, "password", password));

            // 步骤1：查找用户
            User user = userMapper.findByUsername(username);
            if (user == null) {
                result.put("step1", "❌ 用户不存在");
                return Result.success(result);
            }
            result.put("step1", "✅ 用户存在");
            result.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "status", user.getStatus()
            ));

            // 步骤2：检查用户状态
            if (user.getStatus() == 0) {
                result.put("step2", "❌ 账号已被禁用");
                return Result.success(result);
            }
            result.put("step2", "✅ 账号状态正常");

            // 步骤3：验证密码
            String inputWithSalt = password + user.getSalt();
            boolean passwordMatch = passwordEncoder.matches(inputWithSalt, user.getPasswordHash());
            result.put("step3", passwordMatch ? "✅ 密码验证成功" : "❌ 密码验证失败");
            result.put("passwordMatch", passwordMatch);

            if (!passwordMatch) {
                return Result.success(result);
            }

            // 步骤4：尝试调用实际的login方法
            try {
                com.codivio.dto.auth.LoginRequestDTO loginRequest = new com.codivio.dto.auth.LoginRequestDTO();
                loginRequest.setUsername(username);
                loginRequest.setPassword(password);

                com.codivio.dto.auth.LoginResponseDTO loginResponse = userService.login(loginRequest);
                result.put("step4", "✅ 登录方法调用成功");
                result.put("loginResponse", Map.of(
                        "hasAccessToken", loginResponse.getAccessToken() != null,
                        "hasRefreshToken", loginResponse.getRefreshToken() != null,
                        "expiresIn", loginResponse.getExpiresIn()
                ));

            } catch (Exception e) {
                result.put("step4", "❌ 登录方法调用失败: " + e.getMessage());
                result.put("loginError", e.getClass().getSimpleName());
            }

        } catch (Exception e) {
            log.error("登录调试失败", e);
            result.put("error", e.getMessage());
            return Result.error("登录调试失败: " + e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 调试密码验证
     */
    @GetMapping("/test-password")
    public Result<Map<String, Object>> testPassword(@RequestParam String username,
                                                    @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userMapper == null) {
                return Result.error("UserMapper未注入");
            }

            // 查找用户
            User user = userMapper.findByUsername(username);
            if (user == null) {
                result.put("step1", "❌ 用户不存在");
                return Result.success(result);
            }

            result.put("step1", "✅ 用户存在");
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("status", user.getStatus());

            // 检查密码
            String inputWithSalt = password + user.getSalt();
            result.put("step2", "✅ 密码+盐值生成");
            result.put("inputWithSalt", inputWithSalt);
            result.put("storedHash", user.getPasswordHash());
            result.put("salt", user.getSalt());

            // 验证密码
            boolean passwordMatch = passwordEncoder.matches(inputWithSalt, user.getPasswordHash());
            result.put("step3", passwordMatch ? "✅ 密码验证成功" : "❌ 密码验证失败");
            result.put("passwordMatch", passwordMatch);

            // 测试新密码编码
            String newHash = passwordEncoder.encode(inputWithSalt);
            result.put("step4", "✅ 新密码哈希生成");
            result.put("newHash", newHash);

            // 验证新哈希
            boolean newHashMatch = passwordEncoder.matches(inputWithSalt, newHash);
            result.put("step5", newHashMatch ? "✅ 新哈希验证成功" : "❌ 新哈希验证失败");

        } catch (Exception e) {
            log.error("密码测试失败", e);
            result.put("error", e.getMessage());
            return Result.error("密码测试失败: " + e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 测试数据库连接
     */
    @GetMapping("/test-db")
    public Result<Map<String, Object>> testDatabase() {
        Map<String, Object> result = new HashMap<>();

        try {
            if (dataSource == null) {
                result.put("dataSource", "❌ DataSource未注入");
                return Result.error("DataSource未配置");
            }

            try (Connection connection = dataSource.getConnection()) {
                result.put("connection", "✅ 数据库连接成功");
                result.put("url", connection.getMetaData().getURL());
                result.put("username", connection.getMetaData().getUserName());
            }

        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            result.put("connection", "❌ 数据库连接失败: " + e.getMessage());
            return Result.error("数据库连接失败: " + e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 测试MyBatis配置
     */
    @GetMapping("/test-mybatis")
    public Result<Map<String, Object>> testMyBatis() {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userMapper == null) {
                result.put("userMapper", "❌ UserMapper未注入");
                return Result.error("MyBatis配置有问题");
            }

            result.put("userMapper", "✅ UserMapper注入成功");

            // 测试查询
            Long count = userMapper.selectCount(null);
            result.put("userCount", count);
            result.put("query", "✅ 查询执行成功");

        } catch (Exception e) {
            log.error("MyBatis测试失败", e);
            result.put("mybatis", "❌ MyBatis测试失败: " + e.getMessage());
            return Result.error("MyBatis测试失败: " + e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 测试用户表查询
     */
    @GetMapping("/test-users")
    public Result<Map<String, Object>> testUsers() {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userMapper == null) {
                return Result.error("UserMapper未注入");
            }

            // 查询用户总数
            Long totalUsers = userMapper.selectCount(null);
            result.put("totalUsers", totalUsers);

            // 查询前5个用户
            List<User> users = userMapper.selectList(null);
            result.put("users", users.stream().limit(5).toList());

        } catch (Exception e) {
            log.error("用户表查询失败", e);
            result.put("error", e.getMessage());
            return Result.error("用户表查询失败: " + e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 测试用户名检查
     */
    @GetMapping("/test-username-check")
    public Result<Map<String, Object>> testUsernameCheck(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (userMapper == null) {
                return Result.error("UserMapper未注入");
            }

            boolean exists = userMapper.existsByUsername(username);
            result.put("username", username);
            result.put("exists", exists);

        } catch (Exception e) {
            log.error("用户名检查失败", e);
            result.put("error", e.getMessage());
            return Result.error("用户名检查失败: " + e.getMessage());
        }

        return Result.success(result);
    }

    /**
     * 系统信息
     */
    @GetMapping("/system-info")
    public Result<Map<String, Object>> systemInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("timestamp", LocalDateTime.now());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("springBootVersion", org.springframework.boot.SpringBootVersion.getVersion());
        info.put("activeProfiles", System.getProperty("spring.profiles.active"));

        return Result.success(info);
    }
}