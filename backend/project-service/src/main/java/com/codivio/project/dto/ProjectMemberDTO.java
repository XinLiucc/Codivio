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
     * 用户名（来自用户服务）
     */
    private String username;
    
    /**
     * 用户邮箱（来自用户服务）
     */
    private String email;
    
    /**
     * 用户昵称（来自用户服务，可能为空）
     */
    private String nickname;
    
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
    
    public ProjectMemberDTO(Long id, Long projectId, Long userId, String username, 
                           String email, String nickname, ProjectRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
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
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
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
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                '}';
    }
}