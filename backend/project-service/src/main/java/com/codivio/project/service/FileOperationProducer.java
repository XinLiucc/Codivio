package com.codivio.project.service;

import com.codivio.project.config.RabbitConfig;
import com.codivio.project.dto.FileOperationMessage;
import com.codivio.project.entity.ProjectFileTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 文件操作消息生产者
 * 负责向文件服务发送异步操作消息
 */
@Service
public class FileOperationProducer {

    private static final Logger logger = LoggerFactory.getLogger(FileOperationProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送创建文件消息
     * 
     * @param fileTreeNode 文件树节点
     * @param userId 操作用户ID
     */
    public void sendCreateFileMessage(ProjectFileTree fileTreeNode, Long userId) {
        FileOperationMessage message = new FileOperationMessage(
            FileOperationMessage.OperationType.CREATE_FILE,
            fileTreeNode.getProjectId(),
            fileTreeNode.getId(),
            fileTreeNode.getFilePath(),
            fileTreeNode.getFileName(),
            userId
        );
        
        message.setMessageId(generateMessageId());
        message.setParentPath(fileTreeNode.getParentPath());
        
        sendMessage(message);
        
        logger.info("已发送创建文件消息: projectId={}, filePath={}, messageId={}", 
                   fileTreeNode.getProjectId(), fileTreeNode.getFilePath(), message.getMessageId());
    }

    /**
     * 发送创建目录消息
     * 
     * @param fileTreeNode 文件树节点
     * @param userId 操作用户ID
     */
    public void sendCreateDirectoryMessage(ProjectFileTree fileTreeNode, Long userId) {
        FileOperationMessage message = new FileOperationMessage(
            FileOperationMessage.OperationType.CREATE_DIRECTORY,
            fileTreeNode.getProjectId(),
            fileTreeNode.getId(),
            fileTreeNode.getFilePath(),
            fileTreeNode.getFileName(),
            userId
        );
        
        message.setMessageId(generateMessageId());
        message.setParentPath(fileTreeNode.getParentPath());
        
        sendMessage(message);
        
        logger.info("已发送创建目录消息: projectId={}, filePath={}, messageId={}", 
                   fileTreeNode.getProjectId(), fileTreeNode.getFilePath(), message.getMessageId());
    }

    /**
     * 发送删除文件消息
     * 
     * @param fileTreeNode 文件树节点
     * @param userId 操作用户ID
     */
    public void sendDeleteFileMessage(ProjectFileTree fileTreeNode, Long userId) {
        FileOperationMessage message = new FileOperationMessage(
            FileOperationMessage.OperationType.DELETE_FILE,
            fileTreeNode.getProjectId(),
            fileTreeNode.getId(),
            fileTreeNode.getFilePath(),
            fileTreeNode.getFileName(),
            userId
        );
        
        message.setMessageId(generateMessageId());
        message.setFileId(fileTreeNode.getFileId());
        
        sendMessage(message);
        
        logger.info("已发送删除文件消息: projectId={}, filePath={}, fileId={}, messageId={}", 
                   fileTreeNode.getProjectId(), fileTreeNode.getFilePath(), 
                   fileTreeNode.getFileId(), message.getMessageId());
    }

    /**
     * 发送删除目录消息
     * 
     * @param fileTreeNode 文件树节点
     * @param userId 操作用户ID
     */
    public void sendDeleteDirectoryMessage(ProjectFileTree fileTreeNode, Long userId) {
        FileOperationMessage message = new FileOperationMessage(
            FileOperationMessage.OperationType.DELETE_DIRECTORY,
            fileTreeNode.getProjectId(),
            fileTreeNode.getId(),
            fileTreeNode.getFilePath(),
            fileTreeNode.getFileName(),
            userId
        );
        
        message.setMessageId(generateMessageId());
        
        sendMessage(message);
        
        logger.info("已发送删除目录消息: projectId={}, filePath={}, messageId={}", 
                   fileTreeNode.getProjectId(), fileTreeNode.getFilePath(), message.getMessageId());
    }

    /**
     * 发送移动文件消息
     * 
     * @param fileTreeNode 文件树节点
     * @param oldFilePath 原文件路径
     * @param userId 操作用户ID
     */
    public void sendMoveFileMessage(ProjectFileTree fileTreeNode, String oldFilePath, Long userId) {
        FileOperationMessage message = new FileOperationMessage(
            FileOperationMessage.OperationType.MOVE_FILE,
            fileTreeNode.getProjectId(),
            fileTreeNode.getId(),
            fileTreeNode.getFilePath(),
            fileTreeNode.getFileName(),
            userId
        );
        
        message.setMessageId(generateMessageId());
        message.setFileId(fileTreeNode.getFileId());
        message.setExtraData("{\"oldFilePath\":\"" + oldFilePath + "\"}");
        
        sendMessage(message);
        
        logger.info("已发送移动文件消息: projectId={}, oldPath={}, newPath={}, messageId={}", 
                   fileTreeNode.getProjectId(), oldFilePath, 
                   fileTreeNode.getFilePath(), message.getMessageId());
    }

    /**
     * 发送重命名文件消息
     * 
     * @param fileTreeNode 文件树节点
     * @param oldFileName 原文件名
     * @param userId 操作用户ID
     */
    public void sendRenameFileMessage(ProjectFileTree fileTreeNode, String oldFileName, Long userId) {
        FileOperationMessage message = new FileOperationMessage(
            FileOperationMessage.OperationType.RENAME_FILE,
            fileTreeNode.getProjectId(),
            fileTreeNode.getId(),
            fileTreeNode.getFilePath(),
            fileTreeNode.getFileName(),
            userId
        );
        
        message.setMessageId(generateMessageId());
        message.setFileId(fileTreeNode.getFileId());
        message.setExtraData("{\"oldFileName\":\"" + oldFileName + "\"}");
        
        sendMessage(message);
        
        logger.info("已发送重命名文件消息: projectId={}, oldName={}, newName={}, messageId={}", 
                   fileTreeNode.getProjectId(), oldFileName, 
                   fileTreeNode.getFileName(), message.getMessageId());
    }

    /**
     * 发送消息到队列
     * 
     * @param message 文件操作消息
     */
    private void sendMessage(FileOperationMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitConfig.FILE_OPERATION_EXCHANGE,
                RabbitConfig.FILE_OPERATION_ROUTING_KEY,
                message
            );
            
            logger.debug("消息发送成功: {}", message);
        } catch (Exception e) {
            logger.error("消息发送失败: {}", message, e);
            throw new RuntimeException("文件操作消息发送失败", e);
        }
    }

    /**
     * 生成唯一消息ID
     * 
     * @return 消息ID
     */
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}