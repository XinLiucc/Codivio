package com.codivio.userservice.filter;

import com.codivio.userservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 
 * 功能说明：
 * 1. 拦截每个HTTP请求，检查Authorization请求头
 * 2. 验证JWT token的有效性
 * 3. 从有效token中提取用户信息
 * 4. 将认证信息设置到Spring Security上下文中
 * 5. 放行请求到下一个过滤器或Controller
 * 
 * 执行时机：
 * - 在Spring Security的UsernamePasswordAuthenticationFilter之前执行
 * - 每个请求只执行一次（OncePerRequestFilter特性）
 * 
 * 处理逻辑：
 * - 无token或token无效：放行，让Spring Security根据路径配置决定是否允许访问
 * - token有效：设置认证状态，绕过后续的认证检查
 * 
 * @author Codivio Team
 * @since 2025-08-20
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * JWT认证过滤器核心处理方法
     * 
     * 处理流程：
     * 1. 提取JWT token → 2. 验证token → 3. 设置认证信息 → 4. 放行请求
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象  
     * @param filterChain 过滤器链，用于将请求传递给下一个过滤器
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 从请求头获取Authorization字段
        String authHeader = request.getHeader("Authorization");

        // 2. 检查Authorization头格式是否正确
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 无token或格式错误，放行到下一个过滤器
            // Spring Security会根据路径配置决定是否允许访问
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取JWT token（去除"Bearer "前缀，共7个字符）
        String token = authHeader.substring(7);

        // 4. 验证token有效性
        if (!jwtUtil.validateToken(token)) {
            // token无效，放行让Spring Security处理
            // 对于需要认证的路径，Security会返回401错误
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 从有效token中提取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);

        // 6. 创建Spring Security认证对象
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                        username,                // 用户名作为principal
                        null,                   // 密码设为null（JWT认证不需要密码）
                        Collections.emptyList() // 权限列表（暂时为空，后续可扩展角色权限）
                );
        
        // 7. 将userId存储到认证对象的details中，方便Controller直接获取
        authToken.setDetails(userId);

        // 8. 将认证信息设置到Spring Security上下文中
        // 这告诉Spring Security："该用户已通过认证"
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 9. 放行请求到下一个过滤器或最终的Controller
        // 此时Spring Security会看到已认证状态，允许访问受保护的资源
        filterChain.doFilter(request, response);
    }
}
