package com.codivio.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 文件操作消息DTO
 * 用于项目服务与文件服务之间的异步通信
 */
public class FileOperationMessage {

    /**
     * 文件操作类型枚举
     */
    public enum OperationType {
        CREATE_FILE("创建文件"),
        DELETE_FILE("删除文件"),
        CREATE_DIRECTORY("创建目录"),
        DELETE_DIRECTORY("删除目录"),
        MOVE_FILE("移动文件"),
        RENAME_FILE("重命名文件"),
        RENAME_DIRECTORY("重命名目录");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 消息ID - 用于幂等性和追踪
     */
    private String messageId;

    /**
     * 操作类型
     */
    private OperationType operationType;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 文件树节点ID
     */
    private Long fileTreeNodeId;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 父路径
     */
    private String parentPath;

    /**
     * 文件服务中的文件ID (用于已存在的文件)
     */
    private String fileId;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 消息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 额外数据 (JSON格式)
     */
    private String extraData;

    // 构造函数
    public FileOperationMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public FileOperationMessage(OperationType operationType, String projectId,
                               Long fileTreeNodeId, String filePath, String fileName, Long userId) {
        this();
        this.operationType = operationType;
        this.projectId = projectId;
        this.fileTreeNodeId = fileTreeNodeId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.userId = userId;
    }

    // Getter和Setter方法

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Long getFileTreeNodeId() {
        return fileTreeNodeId;
    }

    public void setFileTreeNodeId(Long fileTreeNodeId) {
        this.fileTreeNodeId = fileTreeNodeId;
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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    @Override
    public String toString() {
        return "FileOperationMessage{" +
                "messageId='" + messageId + '\'' +
                ", operationType=" + operationType +
                ", projectId=" + projectId +
                ", fileTreeNodeId=" + fileTreeNodeId +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}