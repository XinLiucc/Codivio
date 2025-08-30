package com.codivio.gateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类 (网关版)
 * 负责JWT的生成、解析和验证
 * 
 * @author Claude Code
 */
@Component
public class JwtUtil {
    
    /**
     * JWT密钥 - 从配置文件读取
     * 用于签名和验证JWT
     */
    @Value("${jwt.secret}")
    private String secret;
    
    /**
     * JWT过期时间（秒）- 从配置文件读取
     * 默认24小时 = 24 * 60 * 60 = 86400秒
     */
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    /**
     * JWT发行人 - 标识这个JWT是谁发出的
     */
    @Value("${jwt.issuer:codivio-user-service}")
    private String issuer;
    
    /**
     * 生成JWT Token
     * 用户登录成功后调用此方法
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT字符串
     */
    public String generateToken(Long userId, String username) {
        // 1. 设置时间
        Date now = new Date();                                    // 当前时间
        Date expiryDate = new Date(now.getTime() + expiration * 1000); // 过期时间
        
        // 2. 生成密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        // 3. 构建JWT（新版本API）
        return Jwts.builder()
                .claim("userId", userId)       // 添加自定义声明
                .claim("username", username)   // 添加自定义声明
                .claim("type", "access_token") // Token类型
                .subject(username)             // 设置主题
                .issuer(issuer)               // 设置发行人
                .issuedAt(now)                // 设置签发时间
                .expiration(expiryDate)       // 设置过期时间
                .signWith(key)                // 用密钥签名
                .compact();                   // 生成最终的JWT字符串
    }

    /**
     * 验证JWT是否有效
     * API请求时调用此方法验证Token
     * 
     * @param token JWT字符串
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            // 1. 检查token是否为空
            if (token == null || token.trim().isEmpty()) {
                System.out.println("JWT为空");
                return false;
            }
            
            // 2. 生成密钥（和生成时使用相同的密钥）
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            
            // 3. 解析并验证JWT
            Jwts.parser()
                .verifyWith(key)              // 验证签名
                .requireIssuer(issuer)        // 验证发行人必须匹配
                .build()
                .parseSignedClaims(token);    // 解析JWT，验证失败会抛异常
            
            // 4. 如果执行到这里，说明JWT有效
            return true;
            
        } catch (SecurityException e) {
            // JWT签名无效 - 可能被篡改了
            System.out.println("JWT签名无效: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            // JWT格式错误 - 不是有效的JWT字符串
            System.out.println("JWT格式错误: " + e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            // JWT已过期 - 需要重新登录
            System.out.println("JWT已过期: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            // 不支持的JWT类型
            System.out.println("不支持的JWT: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            // JWT参数错误
            System.out.println("JWT参数错误: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // 其他未知异常
            System.out.println("JWT验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从JWT中获取用户名
     * API处理时获取当前用户身份
     * 
     * @param token JWT字符串
     * @return 用户名，获取失败返回null
     */
    public String getUsernameFromToken(String token) {
        try {
            // 1. 生成密钥
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            
            // 2. 解析JWT获取Claims
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();          // 获取载荷部分
            
            // 3. 从Claims中获取用户名
            return claims.get("username", String.class);
            
        } catch (Exception e) {
            System.out.println("从JWT获取用户名失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT中获取用户ID
     * API处理时获取当前用户ID
     * 
     * @param token JWT字符串
     * @return 用户ID，获取失败返回null
     */
    public Long getUserIdFromToken(String token) {
        try {
            // 1. 生成密钥
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            
            // 2. 解析JWT获取Claims
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 3. 从Claims中获取用户ID
            // 注意：JWT中数字可能被序列化为Integer，需要转换为Long
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else {
                System.out.println("用户ID类型异常: " + userIdObj.getClass());
                return null;
            }
            
        } catch (Exception e) {
            System.out.println("从JWT获取用户ID失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT中获取所有声明信息
     * 获取完整的用户信息
     * 
     * @param token JWT字符串
     * @return Claims对象，包含所有声明信息
     */
    public Claims getClaimsFromToken(String token) {
        try {
            // 1. 生成密钥
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            
            // 2. 解析JWT获取Claims
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
        } catch (Exception e) {
            System.out.println("从JWT获取Claims失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取JWT过期时间（秒）
     * 
     * @return JWT过期时间
     */
    public Long getExpiration() {
        return expiration;
    }
}