-- 文件服务数据库表结构
USE codivio_file;

-- 文件存储表
CREATE TABLE files (
    id VARCHAR(32) PRIMARY KEY COMMENT '文件ID (雪花算法生成)',
    
    -- 文件基本信息
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    
    -- 文件类型
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    file_extension VARCHAR(20) COMMENT '文件扩展名',
    
    -- 关联信息
    project_id VARCHAR(32) NOT NULL COMMENT '所属项目ID',
    uploaded_by BIGINT NOT NULL COMMENT '上传者ID',
    
    -- 文件内容（文本文件）
    content LONGTEXT COMMENT '文件内容（仅文本文件）',
    
    -- 文件元数据
    metadata JSON COMMENT '文件元数据（图片尺寸、视频时长等）',
    
    -- 版本信息
    version INT DEFAULT 1 COMMENT '文件版本号',
    
    -- 状态信息
    status TINYINT DEFAULT 1 COMMENT '状态: 0-删除, 1-正常',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_project_id (project_id),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_file_path (file_path),
    INDEX idx_mime_type (mime_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT '文件存储表';

-- 文件版本历史表
CREATE TABLE file_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id VARCHAR(32) NOT NULL COMMENT '文件ID',
    
    -- 版本信息
    version INT NOT NULL COMMENT '版本号',
    content LONGTEXT COMMENT '该版本的文件内容',
    file_path VARCHAR(500) COMMENT '该版本的存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '该版本的文件大小',
    
    -- 变更信息
    changed_by BIGINT NOT NULL COMMENT '修改者ID',
    change_comment TEXT COMMENT '变更说明',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_file_version (file_id, version),
    INDEX idx_file_id (file_id),
    INDEX idx_version (version),
    INDEX idx_changed_by (changed_by),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
) COMMENT '文件版本历史表';

-- 文件访问日志表
CREATE TABLE file_access_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id VARCHAR(32) NOT NULL COMMENT '文件ID',
    
    -- 访问信息
    user_id BIGINT NOT NULL COMMENT '访问者ID',
    access_type ENUM('download', 'preview', 'edit', 'delete') NOT NULL COMMENT '访问类型',
    
    -- 访问详情
    ip_address VARCHAR(45) COMMENT '访问IP地址',
    user_agent TEXT COMMENT '用户代理',
    
    -- 时间戳
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_file_id (file_id),
    INDEX idx_user_id (user_id),
    INDEX idx_access_type (access_type),
    INDEX idx_accessed_at (accessed_at),
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
) COMMENT '文件访问日志表';