package com.codivio.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 文件操作回调消息DTO
 * 用于文件服务回调项目服务的异步通信
 */
public class FileCallbackMessage {

    /**
     * 回调状态枚举
     */
    public enum CallbackStatus {
        SUCCESS("成功"),
        FAILED("失败"),
        PARTIAL_SUCCESS("部分成功");

        private final String description;

        CallbackStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 原始消息ID - 关联原始操作消息
     */
    private String originalMessageId;

    /**
     * 回调消息ID
     */
    private String callbackMessageId;

    /**
     * 回调状态
     */
    private CallbackStatus status;

    /**
     * 操作类型 - 对应原始操作
     */
    private FileOperationMessage.OperationType operationType;

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
     * 文件服务中的文件ID - 创建成功时返回
     */
    private String fileId;

    /**
     * 文件大小 - 字节数
     */
    private Long fileSize;

    /**
     * 错误信息 - 失败时的详细信息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime callbackTime;

    /**
     * 处理耗时 - 毫秒
     */
    private Long processingTime;

    /**
     * 额外数据 - JSON格式
     */
    private String extraData;

    // 构造函数
    public FileCallbackMessage() {
        this.callbackTime = LocalDateTime.now();
    }

    public FileCallbackMessage(String originalMessageId, CallbackStatus status,
                              FileOperationMessage.OperationType operationType,
                              String projectId, Long fileTreeNodeId) {
        this();
        this.originalMessageId = originalMessageId;
        this.status = status;
        this.operationType = operationType;
        this.projectId = projectId;
        this.fileTreeNodeId = fileTreeNodeId;
    }

    // Getter和Setter方法

    public String getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

    public String getCallbackMessageId() {
        return callbackMessageId;
    }

    public void setCallbackMessageId(String callbackMessageId) {
        this.callbackMessageId = callbackMessageId;
    }

    public CallbackStatus getStatus() {
        return status;
    }

    public void setStatus(CallbackStatus status) {
        this.status = status;
    }

    public FileOperationMessage.OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(FileOperationMessage.OperationType operationType) {
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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public LocalDateTime getCallbackTime() {
        return callbackTime;
    }

    public void setCallbackTime(LocalDateTime callbackTime) {
        this.callbackTime = callbackTime;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    @Override
    public String toString() {
        return "FileCallbackMessage{" +
                "originalMessageId='" + originalMessageId + '\'' +
                ", callbackMessageId='" + callbackMessageId + '\'' +
                ", status=" + status +
                ", operationType=" + operationType +
                ", projectId=" + projectId +
                ", fileTreeNodeId=" + fileTreeNodeId +
                ", filePath='" + filePath + '\'' +
                ", fileId='" + fileId + '\'' +
                ", callbackTime=" + callbackTime +
                '}';
    }
}