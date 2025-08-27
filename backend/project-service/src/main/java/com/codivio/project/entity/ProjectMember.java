package com.codivio.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目成员实体类
 * 对应数据库表：project_members
 */
@Entity
@Table(name = "project_members", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {

    /**
     * 成员记录ID - 主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目ID - 非空，关联projects表
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 用户ID - 非空，关联用户服务的用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 成员角色 - 非空，存储为字符串
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ProjectRole role;

    /**
     * 加入时间 - 非空，不可更新
     */
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // 构造函数
    public ProjectMember() {}

    public ProjectMember(Long projectId, Long userId, ProjectRole role) {
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
    }

    // JPA生命周期回调方法
    
    /**
     * 持久化前回调 - 自动设置加入时间
     */
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}

