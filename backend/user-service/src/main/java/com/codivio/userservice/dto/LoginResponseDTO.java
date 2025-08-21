package com.codivio.userservice.dto;

import com.codivio.userservice.entity.User;

/**
 * 用户登录响应DTO
 * 包含JWT Token和用户信息
 */
public class LoginResponseDTO {
    
    /**
     * JWT访问令牌
     */
    private String token;
    
    /**
     * 用户基本信息（不包含密码等敏感信息）
     */
    private User userInfo;
    
    /**
     * Token过期时间（秒）
     */
    private Long expiresIn;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, User userInfo, Long expiresIn) {
        this.token = token;
        this.userInfo = userInfo;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "LoginResponseDTO{" +
                "token='[PROTECTED]'" +
                ", userInfo=" + userInfo +
                ", expiresIn=" + expiresIn +
                '}';
    }
}