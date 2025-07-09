package com.codivio.config;

import com.codivio.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置类 - 修复403问题
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security过滤器链配置 - 修复Spring Boot 3版本
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(csrf -> csrf.disable())

                // 启用CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 设置会话策略为无状态
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置URL访问权限
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 注意：由于设置了context-path: /api/v1，这里的路径是相对于context-path的
                        .requestMatchers(
                                "/auth/**",                 // 认证相关接口 (实际路径: /api/v1/auth/**)
                                "/health",                  // 健康检查接口 (实际路径: /api/v1/health)
                                "/health/**",               // 健康检查子路径 (实际路径: /api/v1/health/**)
                                "/debug/**",                // 调试接口 (实际路径: /api/v1/debug/**)
                                "/actuator/**",             // actuator接口 (实际路径: /api/v1/actuator/**)
                                "/error",                   // 错误页面
                                "/favicon.ico"              // 网站图标
                        ).permitAll()

                        // WebSocket连接
                        .requestMatchers("/ws/**").permitAll()

                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

        // 允许的头
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // 是否允许携带凭证
        configuration.setAllowCredentials(allowCredentials);

        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}