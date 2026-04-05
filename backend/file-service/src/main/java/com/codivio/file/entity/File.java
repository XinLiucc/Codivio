package com.codivio.file.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件实体类
 * 对应数据库表：files
 */
@Entity
@Table(name = "files")
public class File {

    /**
     * 文件ID - 主键，字符串类型
     */
    @Id
    @Column(name = "id", length = 32)
    private String id;

    /**
     * 原始文件名 - 用户上传时的文件名
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /**
     * 存储文件名 - 系统生成的唯一文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 文件存储路径 - 文件在服务器上的物理路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * 文件扩展名
     */
    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    /**
     * 所属项目ID
     */
    @Column(name = "project_id", nullable = false, length = 32)
    private String projectId;

    /**
     * 文件内容（仅文本文件）
     */
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 文件元数据（JSON格式）
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /**
     * 文件版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * 文件状态：0-删除, 1-正常
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 最后编辑者ID
     */
    @Column(name = "last_editor_id")
    private Long lastEditorId;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 构造函数
    public File() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter and Setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getLastEditorId() {
        return lastEditorId;
    }

    public void setLastEditorId(Long lastEditorId) {
        this.lastEditorId = lastEditorId;
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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", originalName='" + originalName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", projectId=" + projectId +
                ", fileSize=" + fileSize +
                ", version=" + version +
                ", status=" + status +
                '}';
    }
}