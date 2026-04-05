package com.codivio.file.dto;

import java.time.LocalDateTime;

/**
 * 文件响应DTO
 */
public class FileResponseDTO {

    /**
     * 文件ID
     */
    private String id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 格式化的文件大小
     */
    private String fileSizeFormatted;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件类型描述
     */
    private String fileTypeDescription;

    /**
     * 所属项目ID
     */
    private String projectId;


    /**
     * 文件内容（仅文本文件）
     */
    private String content;

    /**
     * 文件版本号
     */
    private Integer version;

    /**
     * 最后编辑者ID
     */
    private Long lastEditorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 下载URL（可选）
     */
    private String downloadUrl;

    public FileResponseDTO() {
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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeFormatted() {
        return fileSizeFormatted;
    }

    public void setFileSizeFormatted(String fileSizeFormatted) {
        this.fileSizeFormatted = fileSizeFormatted;
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

    public String getFileTypeDescription() {
        return fileTypeDescription;
    }

    public void setFileTypeDescription(String fileTypeDescription) {
        this.fileTypeDescription = fileTypeDescription;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "FileResponseDTO{" +
                "id='" + id + '\'' +
                ", originalName='" + originalName + '\'' +
                ", fileSize=" + fileSize +
                ", projectId=" + projectId +
                ", version=" + version +
                '}';
    }
}