package com.codivio.file.service;

import com.codivio.file.config.RabbitConfig;
import com.codivio.file.dto.FileCallbackMessage;
import com.codivio.file.dto.FileOperationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文件操作回调消息生产者
 * 向项目服务发送文件操作结果回调
 */
@Service
public class FileCallbackProducer {

    private static final Logger logger = LoggerFactory.getLogger(FileCallbackProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送文件操作回调消息
     * 
     * @param callbackMessage 回调消息
     */
    public void sendCallback(FileCallbackMessage callbackMessage) {
        try {
            logger.info("发送文件操作回调消息: {}", callbackMessage);

            // 发送到回调队列
            rabbitTemplate.convertAndSend(
                RabbitConfig.FILE_CALLBACK_EXCHANGE,
                RabbitConfig.FILE_CALLBACK_ROUTING_KEY,
                callbackMessage
            );

            logger.info("回调消息发送成功: messageId={}, status={}, fileId={}", 
                       callbackMessage.getOriginalMessageId(),
                       callbackMessage.getStatus(),
                       callbackMessage.getFileId());

        } catch (Exception e) {
            logger.error("发送回调消息失败: {}", callbackMessage, e);
            throw new RuntimeException("发送回调消息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送成功回调
     * 
     * @param originalMessageId 原始消息ID
     * @param operationType 操作类型
     * @param projectId 项目ID
     * @param fileTreeNodeId 文件树节点ID
     * @param filePath 文件路径
     * @param fileId 文件服务中的文件ID
     */
    public void sendSuccessCallback(String originalMessageId,
                                   FileOperationMessage.OperationType operationType,
                                   Long projectId,
                                   Long fileTreeNodeId,
                                   String filePath,
                                   String fileId) {
        FileCallbackMessage callback = FileCallbackMessage.success(
            originalMessageId, operationType, projectId, fileTreeNodeId, filePath, fileId);
        sendCallback(callback);
    }

    /**
     * 发送失败回调
     * 
     * @param originalMessageId 原始消息ID
     * @param operationType 操作类型
     * @param projectId 项目ID
     * @param fileTreeNodeId 文件树节点ID
     * @param filePath 文件路径
     * @param errorMessage 错误消息
     */
    public void sendFailedCallback(String originalMessageId,
                                  FileOperationMessage.OperationType operationType,
                                  Long projectId,
                                  Long fileTreeNodeId,
                                  String filePath,
                                  String errorMessage) {
        FileCallbackMessage callback = FileCallbackMessage.failed(
            originalMessageId, operationType, projectId, fileTreeNodeId, filePath, errorMessage);
        sendCallback(callback);
    }
}