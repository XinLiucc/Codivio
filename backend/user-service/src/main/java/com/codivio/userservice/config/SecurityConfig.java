package com.codivio.userservice.config;

import com.codivio.userservice.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置类
 * 
 * 主要功能：
 * 1. 配置密码加密器（BCrypt）
 * 2. 配置安全过滤器链（路径权限、JWT认证）
 * 3. 配置无状态会话管理
 * 4. 配置CORS跨域支持
 * 
 * @author Codivio Team
 * @since 2025-08-20
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * 密码加密器Bean
     * 使用BCrypt算法，安全性高，自带盐值
     * 
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS跨域资源共享配置
     * 
     * 功能说明：
     * - 解决前端开发时的跨域访问问题
     * - 支持JWT认证信息的跨域传递
     * - 配置预检请求的缓存策略
     * 
     * 工作原理：
     * 1. 浏览器检测到跨域请求时，先发送OPTIONS预检请求
     * 2. 服务器返回允许的来源、方法、头部信息
     * 3. 浏览器验证通过后，发送真实请求
     * 
     * @return CorsConfigurationSource CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的请求来源（开发环境 + 生产环境）
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",     // React开发服务器（npm start）
            "http://localhost:8090",     // Docker前端服务
            "http://127.0.0.1:3000",     // 本地开发备用地址
            "http://127.0.0.1:8090"      // Docker前端备用地址
        ));
        
        // 允许的HTTP请求方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET",      // 查询操作
            "POST",     // 创建操作  
            "PUT",      // 更新操作
            "DELETE",   // 删除操作
            "OPTIONS",  // 预检请求（浏览器自动发送）
            "PATCH"     // 部分更新操作
        ));
        
        // 允许的请求头（* 表示允许所有）
        // 包括：Content-Type, Authorization, X-Requested-With等
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送认证信息（Cookie、Authorization头等）
        // JWT认证必须设置为true
        configuration.setAllowCredentials(true);
        
        // 预检请求结果的缓存时间（秒）
        // 减少重复的OPTIONS请求，提升性能
        configuration.setMaxAge(3600L);
        
        // 允许前端访问的响应头
        // 这些头部信息前端JavaScript可以读取
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",        // JWT令牌
            "Content-Type",         // 响应内容类型
            "X-Requested-With",     // Ajax请求标识
            "X-Total-Count"         // 分页总数（如果有分页功能）
        ));
        
        // 创建基于URL模式的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 将配置应用到所有API路径
        // "/**" 表示匹配所有路径
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    /**
     * 配置Spring Security安全过滤器链
     * 
     * 核心功能：
     * 1. 路径权限控制：配置哪些路径公开访问，哪些需要认证
     * 2. 会话管理：设置为无状态模式，适配JWT认证
     * 3. CSRF保护：禁用CSRF，因为JWT本身具备防护能力
     * 
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 配置路径访问权限
                .authorizeHttpRequests(auth -> auth
                        // 认证相关接口公开访问（注册、登录、检查用户名等）
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 健康检查端点公开访问（用于服务监控）
                        .requestMatchers("/actuator/health").permitAll()
                        // 其他所有接口都需要认证
                        .anyRequest().authenticated()
                )
                // 配置会话管理策略
                .sessionManagement(session -> 
                        // 设置为无状态，不创建HttpSession，适配JWT认证
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置CSRF保护
                .csrf(csrf -> 
                        // 禁用CSRF保护，因为使用JWT进行无状态认证
                        csrf.disable()
                )
                // 配置CORS跨域支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 使用自定义过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 构建SecurityFilterChain对象
                .build();
    }
}