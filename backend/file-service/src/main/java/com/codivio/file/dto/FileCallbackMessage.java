package com.codivio.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 文件操作回调消息DTO
 * 文件服务向项目服务发送的操作结果回调
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
     * 回调消息ID
     */
    private String callbackMessageId;

    /**
     * 原始消息ID - 对应FileOperationMessage的messageId
     */
    private String originalMessageId;

    /**
     * 回调状态
     */
    private CallbackStatus status;

    /**
     * 操作类型 - 来自原始消息
     */
    private FileOperationMessage.OperationType operationType;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 文件树节点ID
     */
    private String fileTreeNodeId;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件服务中的文件ID (成功时返回)
     */
    private String fileId;

    /**
     * 文件大小 (成功时返回)
     */
    private Long fileSize;

    /**
     * 错误消息 (失败时返回)
     */
    private String errorMessage;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime callbackTime;

    /**
     * 额外数据
     */
    private String extraData;

    // 构造函数
    public FileCallbackMessage() {
        this.callbackTime = LocalDateTime.now();
    }

    // 便捷构造函数 - 成功回调
    public static FileCallbackMessage success(String originalMessageId,
                                            FileOperationMessage.OperationType operationType,
                                            String projectId, String fileTreeNodeId,
                                            String filePath, String fileId) {
        FileCallbackMessage callback = new FileCallbackMessage();
        callback.setOriginalMessageId(originalMessageId);
        callback.setStatus(CallbackStatus.SUCCESS);
        callback.setOperationType(operationType);
        callback.setProjectId(projectId);
        callback.setFileTreeNodeId(fileTreeNodeId);
        callback.setFilePath(filePath);
        callback.setFileId(fileId);
        return callback;
    }

    // 便捷构造函数 - 失败回调
    public static FileCallbackMessage failed(String originalMessageId,
                                            FileOperationMessage.OperationType operationType,
                                            String projectId, String fileTreeNodeId,
                                            String filePath, String errorMessage) {
        FileCallbackMessage callback = new FileCallbackMessage();
        callback.setOriginalMessageId(originalMessageId);
        callback.setStatus(CallbackStatus.FAILED);
        callback.setOperationType(operationType);
        callback.setProjectId(projectId);
        callback.setFileTreeNodeId(fileTreeNodeId);
        callback.setFilePath(filePath);
        callback.setErrorMessage(errorMessage);
        return callback;
    }

    // Getter和Setter方法

    public String getCallbackMessageId() {
        return callbackMessageId;
    }

    public void setCallbackMessageId(String callbackMessageId) {
        this.callbackMessageId = callbackMessageId;
    }

    public String getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
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

    public String getFileTreeNodeId() {
        return fileTreeNodeId;
    }

    public void setFileTreeNodeId(String fileTreeNodeId) {
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

    public LocalDateTime getCallbackTime() {
        return callbackTime;
    }

    public void setCallbackTime(LocalDateTime callbackTime) {
        this.callbackTime = callbackTime;
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
                "callbackMessageId='" + callbackMessageId + '\'' +
                ", originalMessageId='" + originalMessageId + '\'' +
                ", status=" + status +
                ", operationType=" + operationType +
                ", projectId=" + projectId +
                ", fileTreeNodeId=" + fileTreeNodeId +
                ", filePath='" + filePath + '\'' +
                ", fileId='" + fileId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", callbackTime=" + callbackTime +
                '}';
    }
}