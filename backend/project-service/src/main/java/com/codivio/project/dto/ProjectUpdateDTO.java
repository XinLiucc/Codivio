package com.codivio.project.dto;

import jakarta.validation.constraints.Size;

/**
 * 项目更新请求DTO
 * 支持部分更新 - 所有字段都是可选的
 */
public class ProjectUpdateDTO {
    
    /**
     * 项目名称（可选）
     * 如果提供，长度限制：1-100个字符
     */
    @Size(min = 1, max = 100, message = "项目名称长度必须在1-100个字符之间")
    private String name;
    
    /**
     * 项目描述（可选）
     * 长度限制：最大1000个字符
     */
    @Size(max = 1000, message = "项目描述长度不能超过1000个字符")
    private String description;
    
    /**
     * 编程语言（可选）
     * 长度限制：最大20个字符
     */
    @Size(max = 20, message = "编程语言长度不能超过20个字符")
    private String language;
    
    // 构造方法
    public ProjectUpdateDTO() {}
    
    public ProjectUpdateDTO(String name, String description, String language) {
        this.name = name;
        this.description = description;
        this.language = language;
    }
    
    // Getter 和 Setter 方法
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
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    /**
     * 检查是否有任何字段需要更新
     * @return true-有字段需要更新，false-没有更新内容
     */
    public boolean hasUpdates() {
        return name != null || description != null || language != null;
    }
    
    @Override
    public String toString() {
        return "ProjectUpdateDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}