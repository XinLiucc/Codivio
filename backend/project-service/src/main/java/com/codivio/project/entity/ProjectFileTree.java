package com.codivio.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目文件树实体类
 * 对应数据库表：project_file_tree
 * 
 * 用于管理项目的层级化文件目录结构，支持文件夹和文件的树形组织
 */
@Entity
@Table(name = "project_file_tree")
public class ProjectFileTree {

    /**
     * 主键ID - 自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目ID - 非空，关联projects表
     */
    @Column(name = "project_id", nullable = false, length = 32)
    private String projectId;

    /**
     * 文件路径 - 非空，完整的文件路径
     * 示例：/src/main/java/App.java, /docs/README.md
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件名 - 非空，不包含路径的纯文件名
     * 示例：App.java, README.md, components（目录名）
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 父级路径 - 可空，根目录的父路径为null
     * 示例：/src/main/java（App.java的父路径）
     */
    @Column(name = "parent_path", length = 500)
    private String parentPath;

    /**
     * 节点类型 - 非空，文件或目录
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FileTreeType type;

    /**
     * 关联的文件服务ID - 可空，仅file类型需要
     * 用于与file-service中的文件实体关联
     */
    @Column(name = "file_id", length = 32)
    private String fileId;

    /**
     * 最后编辑者ID - 可空，记录最后修改这个节点的用户
     */
    @Column(name = "last_editor_id")
    private Long lastEditorId;

    /**
     * 最后编辑时间 - 非空，记录最后修改时间
     */
    @Column(name = "last_edited_at", nullable = false)
    private LocalDateTime lastEditedAt;

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
    public ProjectFileTree() {}

    /**
     * 便捷构造函数 - 创建文件节点
     */
    public ProjectFileTree(String projectId, String filePath, String fileName,
                          String parentPath, FileTreeType type) {
        this.projectId = projectId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.parentPath = parentPath;
        this.type = type;
    }

    // JPA生命周期回调方法

    /**
     * 持久化前回调 - 自动设置时间戳
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (lastEditedAt == null) {
            lastEditedAt = now;
        }
    }

    /**
     * 更新前回调 - 自动更新时间戳
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastEditedAt = LocalDateTime.now();
    }

    // Getter和Setter方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public FileTreeType getType() {
        return type;
    }

    public void setType(FileTreeType type) {
        this.type = type;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Long getLastEditorId() {
        return lastEditorId;
    }

    public void setLastEditorId(Long lastEditorId) {
        this.lastEditorId = lastEditorId;
    }

    public LocalDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(LocalDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
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

    /**
     * 文件树节点类型枚举
     */
    public enum FileTreeType {
        /**
         * 文件类型
         */
        FILE("file"),
        
        /**
         * 目录类型
         */
        DIRECTORY("directory");

        private final String value;

        FileTreeType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        /**
         * 根据字符串值获取枚举
         */
        public static FileTreeType fromValue(String value) {
            for (FileTreeType type : FileTreeType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown FileTreeType: " + value);
        }
    }

    @Override
    public String toString() {
        return "ProjectFileTree{" +
                "id=" + id +
                ", projectId='" + projectId + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", parentPath='" + parentPath + '\'' +
                ", type=" + type +
                ", fileId='" + fileId + '\'' +
                ", lastEditorId=" + lastEditorId +
                ", lastEditedAt=" + lastEditedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}