package com.codivio.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 项目创建请求DTO
 */
public class ProjectCreateDTO {
    
    /**
     * 项目名称
     * 长度限制：1-100个字符（对应数据库限制）
     * 不能为空或空白
     */
    @NotBlank(message = "项目名称不能为空")
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
     * 长度限制：最大20个字符（对应数据库限制）
     */
    @Size(max = 20, message = "编程语言长度不能超过20个字符")
    private String language;
    
    // 构造方法
    public ProjectCreateDTO() {}
    
    public ProjectCreateDTO(String name, String description, String language) {
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
    
    @Override
    public String toString() {
        return "ProjectCreateDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
