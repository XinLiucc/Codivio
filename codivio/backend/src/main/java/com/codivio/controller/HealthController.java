package com.codivio.controller;

import com.codivio.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 服务健康检查
     */
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "codivio-backend");
        health.put("version", "0.1.0-SNAPSHOT");

        return Result.success(health);
    }

    /**
     * 认证测试接口
     */
    @GetMapping("/auth-test")
    public Result<String> authTest() {
        return Result.success("认证测试成功，您已成功通过JWT认证");
    }
}