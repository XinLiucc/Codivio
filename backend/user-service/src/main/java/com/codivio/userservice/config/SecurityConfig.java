package com.codivio.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全相关配置类
 * 主要用于配置密码加密器等安全组件
 */
@Configuration
public class SecurityConfig {
    
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
}