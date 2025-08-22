package com.codivio.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 用户信息更新请求DTO
 * 
 * 设计原则：
 * - 支持部分更新：所有字段都是可选的
 * - 只验证非空字段：如果字段不为空，则进行格式验证
 * - 用户可以只更新需要修改的字段
 * 
 * 使用场景：
 * - 用户只想更新昵称：只传nickname字段
 * - 用户只想更新邮箱：只传email字段
 * - 用户想同时更新多个字段：传入对应字段
 * 
 * 验证规则：
 * - email: 如果提供，必须是有效的邮箱格式
 * - nickname: 如果提供，长度必须在1-50字符之间
 * - avatarUrl: 如果提供，可以是任意字符串（URL格式验证可在业务层进行）
 */
public class UserUpdateDTO {
    
    /**
     * 邮箱地址（可选）
     * 如果提供，必须是有效的邮箱格式，且需要验证唯一性
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 用户昵称（可选）
     * 如果提供，长度必须在1-50字符之间
     */
    @Size(min = 1, max = 50, message = "昵称长度必须在1-50字符之间")
    private String nickname;
    
    /**
     * 头像URL（可选）
     * 可以为空，表示用户不设置头像
     */
    private String avatarUrl;
    
    /**
     * 无参构造函数
     */
    public UserUpdateDTO() {}
    
    /**
     * 全参构造函数
     */
    public UserUpdateDTO(String email, String nickname, String avatarUrl) {
        this.email = email;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }
    
    // ========== Getter 和 Setter 方法 ==========
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    /**
     * toString方法，便于调试
     * 注意：不包含敏感信息
     */
    @Override
    public String toString() {
        return "UserUpdateDTO{" +
                "email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
