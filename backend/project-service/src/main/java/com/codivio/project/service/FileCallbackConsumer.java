package com.codivio.project.service;

import com.codivio.project.config.RabbitConfig;
import com.codivio.project.dto.FileCallbackMessage;
import com.codivio.project.entity.ProjectFileTree;
import com.codivio.project.repository.ProjectFileTreeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 文件操作回调消息消费者
 * 处理文件服务发送的操作结果回调消息
 */
@Service
public class FileCallbackConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FileCallbackConsumer.class);

    @Autowired
    private ProjectFileTreeRepository projectFileTreeRepository;

    /**
     * 处理文件操作回调消息
     * 
     * @param callbackMessage 回调消息
     */
    @RabbitListener(queues = RabbitConfig.FILE_CALLBACK_QUEUE)
    @Transactional
    public void handleFileOperationCallback(FileCallbackMessage callbackMessage) {
        logger.info("收到文件操作回调消息: {}", callbackMessage);
        
        try {
            // 验证消息完整性
            if (!isValidCallbackMessage(callbackMessage)) {
                logger.warn("回调消息不完整，跳过处理: {}", callbackMessage);
                return;
            }

            // 查找对应的文件树节点
            Optional<ProjectFileTree> nodeOpt = projectFileTreeRepository
                .findById(callbackMessage.getFileTreeNodeId());
            
            if (nodeOpt.isEmpty()) {
                logger.warn("找不到对应的文件树节点: nodeId={}", 
                          callbackMessage.getFileTreeNodeId());
                return;
            }

            ProjectFileTree node = nodeOpt.get();
            
            // 根据回调状态处理
            switch (callbackMessage.getStatus()) {
                case SUCCESS:
                    handleSuccessCallback(node, callbackMessage);
                    break;
                case FAILED:
                    handleFailedCallback(node, callbackMessage);
                    break;
                case PARTIAL_SUCCESS:
                    handlePartialSuccessCallback(node, callbackMessage);
                    break;
                default:
                    logger.warn("未知的回调状态: {}", callbackMessage.getStatus());
            }

            logger.info("文件操作回调处理完成: messageId={}, nodeId={}, status={}", 
                       callbackMessage.getOriginalMessageId(),
                       callbackMessage.getFileTreeNodeId(),
                       callbackMessage.getStatus());

        } catch (Exception e) {
            logger.error("处理文件操作回调消息失败: {}", callbackMessage, e);
            // 这里可以考虑重试机制或死信队列处理
        }
    }

    /**
     * 处理成功回调
     * 
     * @param node 文件树节点
     * @param callbackMessage 回调消息
     */
    private void handleSuccessCallback(ProjectFileTree node, FileCallbackMessage callbackMessage) {
        boolean needUpdate = false;

        // 更新fileId（如果是文件创建操作）
        if (StringUtils.hasText(callbackMessage.getFileId()) && 
            !callbackMessage.getFileId().equals(node.getFileId())) {
            node.setFileId(callbackMessage.getFileId());
            needUpdate = true;
            logger.info("更新文件树节点fileId: nodeId={}, fileId={}", 
                       node.getId(), callbackMessage.getFileId());
        }

        // 更新文件大小信息
        if (callbackMessage.getFileSize() != null) {
            // 这里可以添加文件大小字段到ProjectFileTree实体
            // node.setFileSize(callbackMessage.getFileSize());
            // needUpdate = true;
        }

        // 更新最后修改时间
        if (needUpdate) {
            node.setUpdatedAt(LocalDateTime.now());
            projectFileTreeRepository.save(node);
        }

        logger.info("文件操作成功回调处理完成: nodeId={}, operation={}", 
                   node.getId(), callbackMessage.getOperationType());
    }

    /**
     * 处理失败回调
     * 
     * @param node 文件树节点
     * @param callbackMessage 回调消息
     */
    private void handleFailedCallback(ProjectFileTree node, FileCallbackMessage callbackMessage) {
        logger.error("文件操作失败: nodeId={}, operation={}, error={}", 
                    node.getId(), 
                    callbackMessage.getOperationType(),
                    callbackMessage.getErrorMessage());

        // 根据操作类型决定是否需要回滚文件树节点
        switch (callbackMessage.getOperationType()) {
            case CREATE_FILE:
            case CREATE_DIRECTORY:
                // 创建失败 - 可以考虑删除文件树节点或标记为失败状态
                // 但这里我们保留节点，只是不设置fileId，用户可以重试
                logger.warn("文件创建失败，文件树节点保留: nodeId={}", node.getId());
                break;
                
            case DELETE_FILE:
            case DELETE_DIRECTORY:
                // 删除失败 - 文件树节点已删除，但物理文件还在
                // 这种情况需要人工干预或定期清理
                logger.error("物理文件删除失败，但文件树节点已删除: filePath={}, error={}", 
                           callbackMessage.getFilePath(), callbackMessage.getErrorMessage());
                break;
                
            case MOVE_FILE:
            case RENAME_FILE:
                // 移动/重命名失败 - 可能需要回滚文件树的修改
                logger.error("文件移动/重命名失败: nodeId={}, error={}", 
                           node.getId(), callbackMessage.getErrorMessage());
                break;
                
            default:
                logger.warn("未处理的失败操作类型: {}", callbackMessage.getOperationType());
        }
    }

    /**
     * 处理部分成功回调
     * 
     * @param node 文件树节点
     * @param callbackMessage 回调消息
     */
    private void handlePartialSuccessCallback(ProjectFileTree node, FileCallbackMessage callbackMessage) {
        logger.warn("文件操作部分成功: nodeId={}, operation={}, message={}", 
                   node.getId(), 
                   callbackMessage.getOperationType(),
                   callbackMessage.getErrorMessage());

        // 部分成功的处理策略取决于具体的业务需求
        // 这里先按成功处理，但记录警告日志
        handleSuccessCallback(node, callbackMessage);
    }

    /**
     * 验证回调消息的完整性
     * 
     * @param message 回调消息
     * @return 是否有效
     */
    private boolean isValidCallbackMessage(FileCallbackMessage message) {
        return message != null &&
               StringUtils.hasText(message.getOriginalMessageId()) &&
               message.getStatus() != null &&
               message.getOperationType() != null &&
               message.getProjectId() != null &&
               message.getFileTreeNodeId() != null;
    }
}