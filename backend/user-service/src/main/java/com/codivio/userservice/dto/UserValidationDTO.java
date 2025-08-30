package com.codivio.userservice.dto;

/**
 * 用户验证响应DTO
 * 用于服务间通信时的用户验证结果
 */
public class UserValidationDTO {
    
    private Long userId;
    private String username;
    private String email;
    private boolean exists;
    
    // 默认构造函数
    public UserValidationDTO() {
    }
    
    // 构造函数
    public UserValidationDTO(Long userId, String username, String email, boolean exists) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.exists = exists;
    }
    
    // 静态工厂方法：用户存在
    public static UserValidationDTO exists(Long userId, String username, String email) {
        return new UserValidationDTO(userId, username, email, true);
    }
    
    // 静态工厂方法：用户不存在
    public static UserValidationDTO notExists() {
        return new UserValidationDTO(null, null, null, false);
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isExists() {
        return exists;
    }
    
    public void setExists(boolean exists) {
        this.exists = exists;
    }
    
    @Override
    public String toString() {
        return "UserValidationDTO{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", exists=" + exists +
                '}';
    }
}