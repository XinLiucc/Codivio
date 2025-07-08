-- 设置字符集
SET NAMES utf8mb4;

-- 切换到用户数据库
USE codivio_user_dev;

-- 创建用户表（明确指定字符集）
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci UNIQUE NOT NULL COMMENT '邮箱',
    password_hash VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希',
    salt VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码盐值',
    
    display_name VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '显示名称',
    avatar_url VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '头像链接',
    bio TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '个人简介',
    
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    email_verified BOOLEAN DEFAULT FALSE COMMENT '邮箱是否验证',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='用户基础信息表';

-- 插入测试数据（确保中文正确）
INSERT INTO users (username, email, password_hash, salt, display_name, bio) VALUES
('admin', 'admin@codivio.dev', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi85qhzw1TXFb1mwGYxvRZpi', 'salt123', '系统管理员', '负责系统管理和维护工作'),
('demo', 'demo@codivio.dev', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi85qhzw1TXFb1mwGYxvRZpi', 'salt123', '演示用户', '这是一个演示账号，用于功能展示'),
('zhang_san', 'zhangsan@codivio.dev', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi85qhzw1TXFb1mwGYxvRZpi', 'salt123', '张三', '前端开发工程师，专注Vue.js开发'),
('li_si', 'lisi@codivio.dev', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKi85qhzw1TXFb1mwGYxvRZpi', 'salt123', '李四', '后端开发工程师，擅长Spring Boot微服务');

-- 切换到项目数据库
USE codivio_project_dev;

-- 创建项目表
CREATE TABLE projects (
    id VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci PRIMARY KEY COMMENT '项目ID',
    name VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
    description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '项目描述',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    language VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'javascript' COMMENT '编程语言',
    visibility ENUM('private', 'public') DEFAULT 'private' COMMENT '可见性',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_owner_id (owner_id),
    INDEX idx_language (language),
    INDEX idx_status (status)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='项目基础信息表';

-- 插入测试数据
INSERT INTO projects (id, name, description, owner_id, language) VALUES
('proj_demo_001', 'Hello World 项目', '第一个演示项目，包含基础的前端和后端代码', 1, 'javascript'),
('proj_demo_002', 'Vue 待办事项应用', '使用Vue.js开发的待办事项管理应用，包含增删改查功能', 2, 'javascript'),
('proj_demo_003', '在线聊天室', '基于WebSocket的实时聊天应用，支持多人在线聊天', 3, 'typescript'),
('proj_demo_004', 'Spring Boot API', '后端API项目，使用Spring Boot + MyBatis Plus开发', 4, 'java');
