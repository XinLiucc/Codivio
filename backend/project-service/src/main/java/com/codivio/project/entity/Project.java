package com.codivio.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目实体类
 * 对应数据库表：projects
 */
@Entity
@Table(name = "projects")
public class Project {

    /**
     * 项目ID - 主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目名称 - 非空，长度限制100字符
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 项目描述 - 可空，支持长文本
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 项目所有者ID - 非空，关联用户服务的用户ID
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * 主要编程语言 - 可空，默认javascript
     */
    @Column(name = "language", length = 20)
    private String language = "javascript";

    /**
     * 项目状态 - 非空，1:正常 0:已删除
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 创建时间 - 非空，不可更新
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 非空，自动更新
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 构造函数
    public Project() {}

    // JPA生命周期回调方法
    
    /**
     * 持久化前回调 - 自动设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * 更新前回调 - 自动更新时间戳
     */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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