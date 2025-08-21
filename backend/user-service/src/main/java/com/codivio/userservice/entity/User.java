package com.codivio.userservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：users
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * 用户ID - 主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名 - 唯一，非空
     */
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 邮箱 - 唯一，非空
     */
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    /**
     * 密码哈希值 - 非空
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * 昵称 - 可空
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /**
     * 头像链接 - 可空
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * 用户状态：0-禁用，1-正常
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 最后登录时间 - 可空
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间 - 非空，不可更新
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 非空
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 构造函数
    public User() {}

    // JPA生命周期回调方法
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}