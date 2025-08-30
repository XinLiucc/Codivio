package com.codivio.gateway.filter;

import com.codivio.gateway.client.UserServiceClient;
import com.codivio.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 网关JWT认证过滤器
 * 在所有请求进入网关时验证JWT Token
 * 
 * @author Claude Code
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 不需要认证的路径白名单
     * 这些路径可以直接访问，无需JWT认证
     */
    private static final List<String> WHITELIST_PATHS = List.of(
            "/api/v1/auth/register",     // 用户注册
            "/api/v1/auth/login",        // 用户登录
            "/api/v1/auth/check-username", // 检查用户名
            "/actuator/health"           // 健康检查
    );

    /**
     * 过滤器的核心逻辑
     * 
     * @param exchange 包含请求和响应信息
     * @param chain    过滤器链
     * @return Mono<Void> 响应式返回类型
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 检查是否在白名单中
        if (isWhiteListPath(path)) {
            System.out.println("白名单路径，跳过JWT验证: " + path);
            return chain.filter(exchange);
        }

        // 2. 提取JWT Token
        String token = extractToken(request);
        if (token == null) {
            System.out.println("未找到JWT Token: " + path);
            return unauthorizedResponse(exchange.getResponse(), "缺少认证Token");
        }

        // 3. 验证JWT Token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("JWT Token验证失败: " + path);
            return unauthorizedResponse(exchange.getResponse(), "Token无效或已过期");
        }

        // 4. 提取用户信息并验证用户存在性
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            if (userId != null && username != null) {
                // 5. 调用用户服务验证用户ID和用户名匹配
                return userServiceClient.validateUser(userId, username)
                        .flatMap(isValid -> {
                            if (!isValid) {
                                System.out.println("用户验证失败: " + username + " (ID: " + userId + ")");
                                return unauthorizedResponse(exchange.getResponse(), "用户不存在或信息不匹配");
                            }
                            
                            // 添加用户信息到请求头，移除Authorization头（下游服务不需要JWT）
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", userId.toString())
                                    .header("X-Username", username)
                                    .headers(headers -> headers.remove("Authorization"))
                                    .build();

                            System.out.println("JWT验证成功，用户验证通过: " + username + " (ID: " + userId + ")");
                            System.out.println("传递给下游服务的请求头: X-User-Id=" + userId + ", X-Username=" + username);
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        });
            } else {
                System.out.println("从JWT中提取用户信息失败");
                return unauthorizedResponse(exchange.getResponse(), "Token中用户信息无效");
            }

        } catch (Exception e) {
            System.out.println("处理JWT时发生异常: " + e.getMessage());
            return unauthorizedResponse(exchange.getResponse(), "认证处理异常");
        }
    }

    /**
     * 检查路径是否在白名单中
     * 
     * @param path 请求路径
     * @return true-在白名单中，false-不在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITELIST_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求中提取JWT Token
     * 支持两种方式：
     * 1. Authorization: Bearer <token>
     * 2. 查询参数: ?token=<token>
     * 
     * @param request HTTP请求
     * @return JWT Token字符串，未找到返回null
     */
    private String extractToken(ServerHttpRequest request) {
        // 方式1: 从Authorization请求头获取
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 去掉"Bearer "前缀
        }

        // 方式2: 从查询参数获取（用于某些特殊场景，如WebSocket连接）
        String tokenParam = request.getQueryParams().getFirst("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }

        return null;
    }

    /**
     * 返回401未授权响应
     * 
     * @param response HTTP响应
     * @param message  错误信息
     * @return Mono<Void> 响应式返回类型
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis()
        );

        return response.writeWith(Mono.just(response.bufferFactory().wrap(jsonResponse.getBytes())));
    }

    /**
     * 设置过滤器的执行顺序
     * 数值越小，优先级越高
     * 
     * @return 执行顺序
     */
    @Override
    public int getOrder() {
        return -100; // 设置较高优先级，确保在其他过滤器之前执行
    }
}