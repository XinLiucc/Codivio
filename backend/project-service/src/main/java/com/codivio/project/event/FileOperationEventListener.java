package com.codivio.project.event;

import com.codivio.project.service.FileOperationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * 文件操作事件监听器
 * 使用@TransactionalEventListener确保在数据库事务提交后执行文件操作
 */
@Component
public class FileOperationEventListener {

    @Autowired
    private FileOperationProducer fileOperationProducer;

    /**
     * 处理文件操作事件
     * 在数据库事务提交后异步执行文件操作
     *
     * @param event 文件操作事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileOperationEvent(FileOperationEvent event) {
        try {
            switch (event.getOperationType()) {
                case CREATE_FILE:
                    fileOperationProducer.sendCreateFileMessage(event.getFileTreeNode(), event.getUserId());
                    System.out.println("事务提交后发送创建文件消息: " + event.getFileTreeNode().getFilePath());
                    break;
                    
                case CREATE_DIRECTORY:
                    fileOperationProducer.sendCreateDirectoryMessage(event.getFileTreeNode(), event.getUserId());
                    System.out.println("事务提交后发送创建目录消息: " + event.getFileTreeNode().getFilePath());
                    break;
                    
                case RENAME_FILE:
                    fileOperationProducer.sendRenameFileMessage(
                        event.getFileTreeNode(), 
                        event.getOldFilePath(), 
                        event.getUserId()
                    );
                    System.out.println("事务提交后发送重命名文件消息: " + event.getOldFilePath() + " -> " + event.getFileTreeNode().getFilePath());
                    break;
                    
                case RENAME_DIRECTORY:
                    fileOperationProducer.sendRenameDirectoryMessage(
                        event.getFileTreeNode(), 
                        event.getOldFilePath(), 
                        event.getUserId()
                    );
                    System.out.println("事务提交后发送重命名目录消息: " + event.getOldFilePath() + " -> " + event.getFileTreeNode().getFilePath());
                    break;
                    
                case DELETE_FILE:
                    fileOperationProducer.sendDeleteFileMessage(event.getFileTreeNode(), event.getUserId());
                    System.out.println("事务提交后发送删除文件消息: " + event.getFileTreeNode().getFilePath());
                    break;
                    
                case DELETE_DIRECTORY:
                    fileOperationProducer.sendDeleteDirectoryMessage(event.getFileTreeNode(), event.getUserId());
                    System.out.println("事务提交后发送删除目录消息: " + event.getFileTreeNode().getFilePath());
                    break;
                    
                default:
                    System.err.println("未知的文件操作类型: " + event.getOperationType());
                    break;
            }
        } catch (Exception e) {
            System.err.println("文件操作事件处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}