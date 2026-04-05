package com.codivio.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 文件上传请求DTO
 */
public class FileUploadRequestDTO {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private String projectId;

    /**
     * 文件在项目中的逻辑路径
     * 例如：/src/main.js
     */
    @NotBlank(message = "文件路径不能为空")
    private String filePath;

    /**
     * 文件内容（文本文件）
     */
    private String content;

    /**
     * 编辑者ID（从JWT中获取，可选）
     */
    private Long editorId;

    public FileUploadRequestDTO() {
    }

    public FileUploadRequestDTO(String projectId, String filePath, String content) {
        this.projectId = projectId;
        this.filePath = filePath;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getEditorId() {
        return editorId;
    }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
    }

    @Override
    public String toString() {
        return "FileUploadRequestDTO{" +
                "projectId=" + projectId +
                ", filePath='" + filePath + '\'' +
                ", editorId=" + editorId +
                '}';
    }
}