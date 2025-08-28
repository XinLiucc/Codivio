package com.codivio.project.entity;

/**
 * 项目成员角色枚举
 * 定义项目中成员的权限级别
 */
public enum ProjectRole {
    
    /**
     * 项目所有者 - 拥有项目的完全控制权
     * 权限：项目管理、成员管理、内容编辑、内容查看
     */
    OWNER("项目所有者"),
    
    /**
     * 编辑者 - 可以编辑项目内容
     * 权限：内容编辑、内容查看
     */
    EDITOR("编辑者"),
    
    /**
     * 查看者 - 只能查看项目内容
     * 权限：内容查看
     */
    VIEWER("查看者");
    
    private final String description;
    
    ProjectRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 检查是否有项目管理权限
     * @return true-有管理权限，false-无管理权限
     */
    public boolean canManageProject() {
        return this == OWNER;
    }
    
    /**
     * 检查是否有内容编辑权限
     * @return true-可以编辑，false-只能查看
     */
    public boolean canEditContent() {
        return this == OWNER || this == EDITOR;
    }
}
