package com.codivio.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 生成访问Token
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, expiration, "access");
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, refreshExpiration, "refresh");
    }

    /**
     * 生成Token
     */
    private String generateToken(Long userId, String username, Long expiration, String type) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("username", username)
                .withClaim("type", type)
                .withIssuedAt(new Date())
                .withExpiresAt(expirationDate)
                .sign(algorithm);
    }

    /**
     * 验证Token
     */
    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("Token验证失败: {}", e.getMessage());
            throw new JWTVerificationException("Token验证失败");
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return Long.valueOf(decodedJWT.getSubject());
        } catch (Exception e) {
            log.error("从Token中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getClaim("username").asString();
        } catch (Exception e) {
            log.error("从Token中获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查Token类型
     */
    public boolean isAccessToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            String type = decodedJWT.getClaim("type").asString();
            return "access".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从HTTP请求头中提取Token
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}