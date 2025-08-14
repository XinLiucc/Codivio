-- 项目服务数据库表结构
USE codivio_project;

-- 项目基础信息表
CREATE TABLE projects (
    id VARCHAR(32) PRIMARY KEY COMMENT '项目ID (雪花算法生成)',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    
    -- 所有者信息
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    
    -- 项目配置
    language VARCHAR(20) DEFAULT 'javascript' COMMENT '主要编程语言',
    
    -- 统计信息
    member_count INT DEFAULT 1 COMMENT '成员数量',
    file_count INT DEFAULT 0 COMMENT '文件数量',
    
    -- 状态信息
    status TINYINT DEFAULT 1 COMMENT '状态: 0-删除, 1-正常',
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后活动时间',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_owner_id (owner_id),
    INDEX idx_language (language),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_last_activity (last_activity_at)
) COMMENT '项目基础信息表';

-- 项目成员表
CREATE TABLE project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(32) NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 权限信息
    role ENUM('owner', 'editor', 'viewer') DEFAULT 'editor' COMMENT '角色',
    
    -- 时间信息
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
    
    UNIQUE KEY unique_member (project_id, user_id),
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) COMMENT '项目成员表';

-- 项目文件表
CREATE TABLE project_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(32) NOT NULL COMMENT '项目ID',
    
    -- 文件信息
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    content LONGTEXT COMMENT '文件内容',
    size INT DEFAULT 0 COMMENT '文件大小(字节)',
    
    -- 文件类型
    type ENUM('file', 'directory') DEFAULT 'file' COMMENT '类型',
    
    -- 编辑信息
    last_editor_id BIGINT COMMENT '最后编辑者ID',
    last_edited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后编辑时间',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_file (project_id, file_path),
    INDEX idx_project_id (project_id),
    INDEX idx_type (type),
    INDEX idx_updated_at (updated_at),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) COMMENT '项目文件表';