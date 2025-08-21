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

/**
 * Spring Security 安全配置类
 * 
 * 主要功能：
 * 1. 配置密码加密器（BCrypt）
 * 2. 配置安全过滤器链（路径权限、JWT认证）
 * 3. 配置无状态会话管理
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
                // 使用自定义过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 构建SecurityFilterChain对象
                .build();
    }
}