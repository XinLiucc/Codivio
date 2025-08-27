package com.codivio.project.dto;

import com.codivio.project.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

/**
 * 添加项目成员请求DTO
 * 用于接收添加成员的请求参数
 */
public class AddMemberDTO {
    
    /**
     * 用户ID
     * 必填字段，指定要添加到项目的用户
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 成员角色
     * 必填字段，指定用户在项目中的角色
     */
    @NotNull(message = "成员角色不能为空")
    private ProjectRole role;
    
    // 构造方法
    public AddMemberDTO() {}
    
    public AddMemberDTO(Long userId, ProjectRole role) {
        this.userId = userId;
        this.role = role;
    }
    
    // Getter 和 Setter 方法
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
    
    @Override
    public String toString() {
        return "AddMemberDTO{" +
                "userId=" + userId +
                ", role=" + role +
                '}';
    }
}
