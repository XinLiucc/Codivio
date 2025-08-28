package com.codivio.project.dto;

import com.codivio.project.entity.ProjectRole;
import java.time.LocalDateTime;

/**
 * 项目成员响应DTO
 * 用于返回项目成员信息（包含用户基本信息）
 */
public class ProjectMemberDTO {
    
    /**
     * 成员记录ID
     */
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 成员角色
     */
    private ProjectRole role;
    
    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;
    
    // 构造方法
    public ProjectMemberDTO() {}
    
    public ProjectMemberDTO(Long id, Long projectId, Long userId, ProjectRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }
    
    // Getter 和 Setter 方法
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
    
    @Override
    public String toString() {
        return "ProjectMemberDTO{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                '}';
    }
}