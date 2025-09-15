package com.codivio.file.service;

import com.codivio.file.config.RabbitConfig;
import com.codivio.file.dto.FileCallbackMessage;
import com.codivio.file.dto.FileOperationMessage;
import com.codivio.file.entity.File;
import com.codivio.file.repository.FileRepository;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 文件操作消息消费者
 * 处理来自项目服务的文件操作请求
 */
@Service
public class FileOperationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FileOperationConsumer.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileCallbackProducer fileCallbackProducer;

    @Value("${file.storage.path:/app/files}")
    private String fileStoragePath;

    /**
     * 处理文件操作消息
     * 
     * @param operationMessage 文件操作消息
     * @param message AMQP消息
     * @param channel AMQP通道
     */
    @RabbitListener(queues = RabbitConfig.FILE_OPERATION_QUEUE)
    @Transactional
    public void handleFileOperation(FileOperationMessage operationMessage, 
                                  Message message, Channel channel) {
        logger.info("收到文件操作消息: {}", operationMessage);
        
        try {
            // 验证消息完整性
            if (!isValidOperationMessage(operationMessage)) {
                logger.warn("文件操作消息不完整，拒绝处理: {}", operationMessage);
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                return;
            }

            // 根据操作类型处理
            FileCallbackMessage callback = null;
            switch (operationMessage.getOperationType()) {
                case CREATE_FILE:
                    callback = handleCreateFile(operationMessage);
                    break;
                case CREATE_DIRECTORY:
                    callback = handleCreateDirectory(operationMessage);
                    break;
                case DELETE_FILE:
                    callback = handleDeleteFile(operationMessage);
                    break;
                case DELETE_DIRECTORY:
                    callback = handleDeleteDirectory(operationMessage);
                    break;
                default:
                    logger.warn("不支持的操作类型: {}", operationMessage.getOperationType());
                    callback = FileCallbackMessage.failed(
                        operationMessage.getMessageId(),
                        operationMessage.getOperationType(),
                        operationMessage.getProjectId(),
                        operationMessage.getFileTreeNodeId(),
                        operationMessage.getFilePath(),
                        "不支持的操作类型: " + operationMessage.getOperationType()
                    );
            }

            // 发送回调消息
            if (callback != null) {
                callback.setCallbackMessageId(UUID.randomUUID().toString());
                fileCallbackProducer.sendCallback(callback);
            }

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            
            logger.info("文件操作处理完成: messageId={}, operation={}, status={}", 
                       operationMessage.getMessageId(),
                       operationMessage.getOperationType(),
                       callback != null ? callback.getStatus() : "UNKNOWN");

        } catch (Exception e) {
            logger.error("处理文件操作消息失败: {}", operationMessage, e);
            try {
                // 发送失败回调
                FileCallbackMessage failedCallback = FileCallbackMessage.failed(
                    operationMessage.getMessageId(),
                    operationMessage.getOperationType(),
                    operationMessage.getProjectId(),
                    operationMessage.getFileTreeNodeId(),
                    operationMessage.getFilePath(),
                    "处理异常: " + e.getMessage()
                );
                failedCallback.setCallbackMessageId(UUID.randomUUID().toString());
                fileCallbackProducer.sendCallback(failedCallback);

                // 拒绝消息，不重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception callbackEx) {
                logger.error("发送失败回调也失败了", callbackEx);
            }
        }
    }

    /**
     * 处理文件创建操作
     */
    private FileCallbackMessage handleCreateFile(FileOperationMessage message) {
        try {
            logger.info("开始创建文件: projectId={}, filePath={}", 
                       message.getProjectId(), message.getFilePath());

            // 创建文件实体记录
            File fileEntity = new File();
            fileEntity.setId(UUID.randomUUID().toString().replace("-", ""));
            fileEntity.setOriginalName(message.getFileName());
            fileEntity.setFileName(message.getFileName());
            fileEntity.setFilePath(message.getFilePath());
            fileEntity.setFileSize(0L);
            fileEntity.setProjectId(message.getProjectId());
            fileEntity.setLastEditorId(message.getUserId());
            fileEntity.setCreatedAt(LocalDateTime.now());
            fileEntity.setUpdatedAt(LocalDateTime.now());

            // 保存到数据库
            File savedEntity = fileRepository.save(fileEntity);

            // 创建物理文件
            String physicalPath = createPhysicalFile(savedEntity.getId(), message.getFilePath());

            logger.info("文件创建成功: fileId={}, physicalPath={}", 
                       savedEntity.getId(), physicalPath);

            // 返回成功回调
            FileCallbackMessage callback = FileCallbackMessage.success(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                savedEntity.getId()
            );
            callback.setFileSize(0L);
            return callback;

        } catch (Exception e) {
            logger.error("创建文件失败: {}", message, e);
            return FileCallbackMessage.failed(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                "创建文件失败: " + e.getMessage()
            );
        }
    }

    /**
     * 处理目录创建操作
     */
    private FileCallbackMessage handleCreateDirectory(FileOperationMessage message) {
        try {
            logger.info("开始创建目录: projectId={}, filePath={}", 
                       message.getProjectId(), message.getFilePath());

            // 创建物理目录
            String physicalPath = createPhysicalDirectory(message.getFilePath());

            logger.info("目录创建成功: physicalPath={}", physicalPath);

            // 目录创建不需要fileId，返回成功回调
            return FileCallbackMessage.success(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                null // 目录没有fileId
            );

        } catch (Exception e) {
            logger.error("创建目录失败: {}", message, e);
            return FileCallbackMessage.failed(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                "创建目录失败: " + e.getMessage()
            );
        }
    }

    /**
     * 处理文件删除操作
     */
    private FileCallbackMessage handleDeleteFile(FileOperationMessage message) {
        try {
            logger.info("开始删除文件: projectId={}, filePath={}", 
                       message.getProjectId(), message.getFilePath());

            // 如果有fileId，从数据库删除记录并删除物理文件
            if (StringUtils.hasText(message.getFileId())) {
                String fileId = message.getFileId();
                File fileEntity = fileRepository.findById(fileId).orElse(null);
                
                if (fileEntity != null) {
                    // 删除物理文件
                    deletePhysicalFile(message.getFileId());
                    
                    // 删除数据库记录
                    fileRepository.deleteById(fileId);
                }
            }

            logger.info("文件删除成功: fileId={}", message.getFileId());

            // 返回成功回调
            return FileCallbackMessage.success(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                null
            );

        } catch (Exception e) {
            logger.error("删除文件失败: {}", message, e);
            return FileCallbackMessage.failed(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                "删除文件失败: " + e.getMessage()
            );
        }
    }

    /**
     * 处理目录删除操作
     */
    private FileCallbackMessage handleDeleteDirectory(FileOperationMessage message) {
        try {
            logger.info("开始删除目录: projectId={}, filePath={}", 
                       message.getProjectId(), message.getFilePath());

            // 删除物理目录
            deletePhysicalDirectory(message.getFilePath());

            logger.info("目录删除成功: filePath={}", message.getFilePath());

            // 返回成功回调
            return FileCallbackMessage.success(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                null
            );

        } catch (Exception e) {
            logger.error("删除目录失败: {}", message, e);
            return FileCallbackMessage.failed(
                message.getMessageId(),
                message.getOperationType(),
                message.getProjectId(),
                message.getFileTreeNodeId(),
                message.getFilePath(),
                "删除目录失败: " + e.getMessage()
            );
        }
    }

    /**
     * 创建物理文件
     */
    private String createPhysicalFile(String fileId, String filePath) throws IOException {
        Path fullPath = Paths.get(fileStoragePath, "project_files", fileId + ".txt");
        
        // 确保父目录存在
        Files.createDirectories(fullPath.getParent());
        
        // 创建空文件
        Files.createFile(fullPath);
        
        return fullPath.toString();
    }

    /**
     * 创建物理目录
     */
    private String createPhysicalDirectory(String dirPath) throws IOException {
        Path fullPath = Paths.get(fileStoragePath, "project_dirs", dirPath.replace("/", "_"));
        
        // 创建目录
        Files.createDirectories(fullPath);
        
        return fullPath.toString();
    }

    /**
     * 删除物理文件
     */
    private void deletePhysicalFile(String fileId) throws IOException {
        Path fullPath = Paths.get(fileStoragePath, "project_files", fileId + ".txt");
        
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
    }

    /**
     * 删除物理目录
     */
    private void deletePhysicalDirectory(String dirPath) throws IOException {
        Path fullPath = Paths.get(fileStoragePath, "project_dirs", dirPath.replace("/", "_"));
        
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
    }

    /**
     * 验证操作消息的完整性
     */
    private boolean isValidOperationMessage(FileOperationMessage message) {
        return message != null &&
               StringUtils.hasText(message.getMessageId()) &&
               message.getOperationType() != null &&
               message.getProjectId() != null &&
               message.getFileTreeNodeId() != null &&
               StringUtils.hasText(message.getFilePath()) &&
               StringUtils.hasText(message.getFileName());
    }
}