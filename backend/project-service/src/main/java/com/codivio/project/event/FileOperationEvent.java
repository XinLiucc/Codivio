package com.codivio.project.event;

import com.codivio.project.entity.ProjectFileTree;
import org.springframework.context.ApplicationEvent;

/**
 * 文件操作事件
 * 用于在数据库事务提交后触发异步文件操作
 */
public class FileOperationEvent extends ApplicationEvent {

    public enum OperationType {
        CREATE_FILE("创建文件"),
        CREATE_DIRECTORY("创建目录"),
        RENAME_FILE("重命名文件"),
        RENAME_DIRECTORY("重命名目录"),
        DELETE_FILE("删除文件"),
        DELETE_DIRECTORY("删除目录");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ProjectFileTree fileTreeNode;
    private final Long userId;
    private final OperationType operationType;
    private final String oldFilePath;

    public FileOperationEvent(Object source, ProjectFileTree fileTreeNode, Long userId, OperationType operationType) {
        super(source);
        this.fileTreeNode = fileTreeNode;
        this.userId = userId;
        this.operationType = operationType;
        this.oldFilePath = null;
    }

    public FileOperationEvent(Object source, ProjectFileTree fileTreeNode, Long userId, 
                            OperationType operationType, String oldFilePath) {
        super(source);
        this.fileTreeNode = fileTreeNode;
        this.userId = userId;
        this.operationType = operationType;
        this.oldFilePath = oldFilePath;
    }

    public ProjectFileTree getFileTreeNode() {
        return fileTreeNode;
    }

    public Long getUserId() {
        return userId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public String getOldFilePath() {
        return oldFilePath;
    }
}