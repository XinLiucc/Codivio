# 数据库设计

## 📊 数据库架构概览

### 数据存储分层
```
┌─────────────────────────────────────────────────────────────┐
│                        应用层                                │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      数据访问层                              │
│  MyBatis Plus | Spring Data Redis | MongoDB Driver        │
└─────────────────────────────────────────────────────────────┘
                              │
    ┌─────────────────────────┼─────────────────────────┐
    │                         │                         │
┌─────────┐            ┌─────────┐            ┌─────────┐
│  MySQL  │            │  Redis  │            │ MongoDB │
│ 关系数据  │            │  缓存   │            │ 文档数据  │
│ 用户项目  │            │ 会话状态 │            │ 日志历史  │
└─────────┘            └─────────┘            └─────────┘
```

## 🗄️ MySQL数据库设计

### 数据库分区策略
```sql
-- 创建用户数据库
CREATE DATABASE codivio_user 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建项目数据库  
CREATE DATABASE codivio_project
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建系统数据库
CREATE DATABASE codivio_system
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 用户数据库表结构

#### users - 用户基础信息表
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
    
    -- 个人信息
    display_name VARCHAR(100) COMMENT '显示名称',
    avatar_url VARCHAR(255) COMMENT '头像链接',
    bio TEXT COMMENT '个人简介',
    location VARCHAR(100) COMMENT '位置',
    website VARCHAR(255) COMMENT '个人网站',
    
    -- 第三方账号
    github_username VARCHAR(50) COMMENT 'GitHub用户名',
    google_id VARCHAR(100) COMMENT 'Google账号ID',
    
    -- 状态信息
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常, 2-待验证',
    email_verified BOOLEAN DEFAULT FALSE COMMENT '邮箱是否验证',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    
    -- 索引
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT '用户基础信息表';
```

#### user_profiles - 用户扩展信息表
```sql
CREATE TABLE user_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 偏好设置
    theme ENUM('light', 'dark', 'auto') DEFAULT 'auto' COMMENT '主题设置',
    language VARCHAR(10) DEFAULT 'zh-CN' COMMENT '界面语言',
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',
    
    -- 编辑器设置
    editor_font_size TINYINT DEFAULT 14 COMMENT '编辑器字体大小',
    editor_font_family VARCHAR(50) DEFAULT 'Monaco' COMMENT '编辑器字体',
    editor_theme VARCHAR(20) DEFAULT 'vs-dark' COMMENT '编辑器主题',
    auto_save BOOLEAN DEFAULT TRUE COMMENT '自动保存',
    
    -- 通知设置
    email_notifications BOOLEAN DEFAULT TRUE COMMENT '邮件通知',
    browser_notifications BOOLEAN DEFAULT TRUE COMMENT '浏览器通知',
    collaboration_alerts BOOLEAN DEFAULT TRUE COMMENT '协作提醒',
    
    -- 隐私设置
    profile_public BOOLEAN DEFAULT TRUE COMMENT '公开个人资料',
    show_email BOOLEAN DEFAULT FALSE COMMENT '显示邮箱',
    allow_project_invites BOOLEAN DEFAULT TRUE COMMENT '允许项目邀请',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_profile (user_id)
) COMMENT '用户扩展信息表';
```

#### user_sessions - 用户会话表
```sql
CREATE TABLE user_sessions (
    id VARCHAR(64) PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 会话信息
    access_token_hash VARCHAR(64) NOT NULL COMMENT 'Access Token哈希',
    refresh_token_hash VARCHAR(64) NOT NULL COMMENT 'Refresh Token哈希',
    device_info JSON COMMENT '设备信息',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT 'User Agent',
    
    -- 时间信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户会话表';
```

### 项目数据库表结构

#### projects - 项目基础信息表
```sql
CREATE TABLE projects (
    id VARCHAR(32) PRIMARY KEY COMMENT '项目ID',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    
    -- 所有者信息
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    
    -- 项目配置
    language VARCHAR(20) DEFAULT 'javascript' COMMENT '主要编程语言',
    template VARCHAR(50) COMMENT '项目模板',
    visibility ENUM('private', 'public', 'internal') DEFAULT 'private' COMMENT '可见性',
    
    -- Git信息
    git_repository VARCHAR(255) COMMENT 'Git仓库路径',
    default_branch VARCHAR(50) DEFAULT 'main' COMMENT '默认分支',
    
    -- 项目设置
    settings JSON COMMENT '项目设置',
    
    -- 统计信息
    file_count INT DEFAULT 0 COMMENT '文件数量',
    commit_count INT DEFAULT 0 COMMENT '提交数量',
    member_count INT DEFAULT 1 COMMENT '成员数量',
    view_count INT DEFAULT 0 COMMENT '访问次数',
    star_count INT DEFAULT 0 COMMENT '收藏次数',
    
    -- 状态信息
    status TINYINT DEFAULT 1 COMMENT '状态: 0-删除, 1-正常, 2-归档',
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后活动时间',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    INDEX idx_owner_id (owner_id),
    INDEX idx_language (language),
    INDEX idx_visibility (visibility),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_last_activity (last_activity_at),
    FOREIGN KEY (owner_id) REFERENCES codivio_user.users(id)
) COMMENT '项目基础信息表';
```

#### project_members - 项目成员表
```sql
CREATE TABLE project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(32) NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 权限信息
    role ENUM('owner', 'admin', 'editor', 'viewer') DEFAULT 'editor' COMMENT '角色',
    permissions JSON COMMENT '具体权限配置',
    
    -- 邀请信息
    invited_by BIGINT COMMENT '邀请人ID',
    invitation_token VARCHAR(64) COMMENT '邀请令牌',
    invitation_status ENUM('pending', 'accepted', 'declined') DEFAULT 'accepted' COMMENT '邀请状态',
    
    -- 时间信息
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    last_accessed_at TIMESTAMP COMMENT '最后访问时间',
    
    UNIQUE KEY unique_member (project_id, user_id),
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES codivio_user.users(id) ON DELETE CASCADE
) COMMENT '项目成员表';
```

#### project_files - 项目文件表
```sql
CREATE TABLE project_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(32) NOT NULL COMMENT '项目ID',
    
    -- 文件信息
    path VARCHAR(500) NOT NULL COMMENT '文件路径',
    name VARCHAR(255) NOT NULL COMMENT '文件名',
    content LONGTEXT COMMENT '文件内容',
    size INT DEFAULT 0 COMMENT '文件大小(字节)',
    
    -- 文件类型
    type ENUM('file', 'directory') DEFAULT 'file' COMMENT '类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    encoding VARCHAR(20) DEFAULT 'utf-8' COMMENT '编码',
    
    -- 版本信息
    version BIGINT DEFAULT 1 COMMENT '版本号',
    checksum VARCHAR(64) COMMENT '文件校验和',
    
    -- 编辑信息
    last_editor_id BIGINT COMMENT '最后编辑者ID',
    last_edited_at TIMESTAMP COMMENT '最后编辑时间',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    UNIQUE KEY unique_file (project_id, path),
    INDEX idx_project_id (project_id),
    INDEX idx_type (type),
    INDEX idx_updated_at (updated_at),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) COMMENT '项目文件表';
```

#### git_commits - Git提交记录表
```sql
CREATE TABLE git_commits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(32) NOT NULL COMMENT '项目ID',
    
    -- 提交信息
    commit_hash VARCHAR(64) NOT NULL COMMENT '提交哈希',
    parent_hashes JSON COMMENT '父提交哈希列表',
    message TEXT NOT NULL COMMENT '提交消息',
    
    -- 作者信息
    author_id BIGINT COMMENT '作者用户ID',
    author_name VARCHAR(100) NOT NULL COMMENT '作者姓名',
    author_email VARCHAR(100) NOT NULL COMMENT '作者邮箱',
    
    -- 提交者信息（可能与作者不同）
    committer_name VARCHAR(100) COMMENT '提交者姓名',
    committer_email VARCHAR(100) COMMENT '提交者邮箱',
    
    -- 统计信息
    files_changed INT DEFAULT 0 COMMENT '变更文件数',
    additions INT DEFAULT 0 COMMENT '新增行数',
    deletions INT DEFAULT 0 COMMENT '删除行数',
    
    -- 分支信息
    branch VARCHAR(100) DEFAULT 'main' COMMENT '所属分支',
    
    -- 时间信息
    commit_time TIMESTAMP NOT NULL COMMENT '提交时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_commit (project_id, commit_hash),
    INDEX idx_project_id (project_id),
    INDEX idx_author_id (author_id),
    INDEX idx_branch (branch),
    INDEX idx_commit_time (commit_time),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) COMMENT 'Git提交记录表';
```

### 系统数据库表结构

#### system_configs - 系统配置表
```sql
CREATE TABLE system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description TEXT COMMENT '配置描述',
    config_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string' COMMENT '配置类型',
    is_encrypted BOOLEAN DEFAULT FALSE COMMENT '是否加密',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_config_key (config_key)
) COMMENT '系统配置表';
```

#### audit_logs - 审计日志表
```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 用户信息
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    
    -- 操作信息
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id VARCHAR(100) COMMENT '资源ID',
    
    -- 请求信息
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT 'User Agent',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    
    -- 操作详情
    old_value JSON COMMENT '操作前数据',
    new_value JSON COMMENT '操作后数据',
    result ENUM('success', 'failure') DEFAULT 'success' COMMENT '操作结果',
    error_message TEXT COMMENT '错误信息',
    
    -- 时间信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_created_at (created_at)
) COMMENT '审计日志表'
PARTITION BY RANGE (UNIX_TIMESTAMP(created_at)) (
    PARTITION p202401 VALUES LESS THAN (UNIX_TIMESTAMP('2024-02-01')),
    PARTITION p202402 VALUES LESS THAN (UNIX_TIMESTAMP('2024-03-01')),
    PARTITION p202403 VALUES LESS THAN (UNIX_TIMESTAMP('2024-04-01')),
    PARTITION future VALUES LESS THAN MAXVALUE
);
```

## 🚀 Redis缓存设计

### 缓存命名规范
```yaml
# 命名格式: {业务模块}:{数据类型}:{唯一标识}:{附加信息}
# 示例:
user:session:12345                    # 用户会话
user:profile:12345                    # 用户资料缓存
project:info:proj_abc123              # 项目信息缓存
editor:document:doc_123               # 文档内容
editor:room:doc_123:users             # 房间用户列表
editor:operation:doc_123:queue        # 操作队列
system:config:app_settings            # 系统配置缓存
```

### 数据库分配
```yaml
# Redis数据库分配
databases:
  0: # 用户会话和认证
    ttl: 7200  # 2小时
    keys:
      - "user:session:{userId}"
      - "user:token:{tokenHash}"
      - "user:online:{userId}"
      
  1: # 项目和文件缓存
    ttl: 3600  # 1小时
    keys:
      - "project:info:{projectId}"
      - "project:members:{projectId}"
      - "project:files:{projectId}"
      
  2: # 实时编辑状态
    ttl: 1800  # 30分钟
    keys:
      - "editor:document:{documentId}"
      - "editor:room:{documentId}:users"
      - "editor:operation:{documentId}:queue"
      - "editor:cursor:{userId}:{documentId}"
      
  3: # 系统配置和统计
    ttl: 86400  # 24小时
    keys:
      - "system:config:{configKey}"
      - "system:stats:{type}:{date}"
      - "system:rate_limit:{userId}:{endpoint}"
```

### 缓存数据结构

#### 用户会话缓存
```json
// Key: user:session:{userId}
{
  "userId": "12345",
  "username": "john_doe",
  "email": "john@example.com",
  "avatar": "https://avatar.codivio.dev/john.png",
  "permissions": ["read", "write", "execute"],
  "preferences": {
    "theme": "dark",
    "language": "zh-CN"
  },
  "loginTime": 1640995200000,
  "lastActiveTime": 1640995200000
}
```

#### 实时文档状态
```json
// Key: editor:document:{documentId}
{
  "documentId": "doc_123",
  "projectId": "proj_abc123",
  "content": "console.log('Hello, Codivio!');",
  "version": 42,
  "lastModified": 1640995200000,
  "activeUsers": {
    "user_123": {
      "userId": "user_123",
      "username": "john_doe",
      "cursorPosition": 150,
      "selection": {"start": 150, "end": 165},
      "color": "#ff6b6b",
      "joinTime": 1640995200000
    }
  }
}
```

#### 操作队列
```json
// Key: editor:operation:{documentId}:queue
// Data Type: List
[
  {
    "id": "op_123",
    "type": "insert",
    "position": 100,
    "text": "Hello, World!",
    "clientId": "client_456",
    "userId": "user_123",
    "timestamp": 1640995200000
  },
  {
    "id": "op_124",
    "type": "delete",
    "position": 90,
    "length": 5,
    "clientId": "client_789",
    "userId": "user_456",
    "timestamp": 1640995201000
  }
]
```

### 缓存策略
```java
@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存穿透防护
    public <T> T getWithNullProtection(String key, Class<T> clazz, Supplier<T> loader) {
        String value = (String) redisTemplate.opsForValue().get(key);
        
        if (value != null) {
            if ("NULL".equals(value)) {
                return null; // 防止缓存穿透
            }
            return JSON.parseObject(value, clazz);
        }
        
        T data = loader.get();
        if (data == null) {
            // 存储空值，防止缓存穿透
            redisTemplate.opsForValue().set(key, "NULL", Duration.ofMinutes(5));
        } else {
            redisTemplate.opsForValue().set(key, JSON.toJSONString(data), Duration.ofHours(1));
        }
        
        return data;
    }
    
    // 分布式锁
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        String result = redisTemplate.execute(new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            String.class), 
            Collections.singletonList(lockKey), 
            requestId
        );
        return "OK".equals(result);
    }
}
```

## 📄 MongoDB文档设计

### 集合设计

#### operation_history - 操作历史集合
```javascript
// 集合索引
db.operation_history.createIndexes([
  { "documentId": 1, "timestamp": 1 },
  { "userId": 1, "timestamp": 1 },
  { "timestamp": 1 },
  { "documentId": 1, "operation.type": 1 }
]);

// 文档结构
{
  "_id": ObjectId("..."),
  "documentId": "doc_123",
  "projectId": "proj_abc123",
  "operation": {
    "id": "op_123456",
    "type": "insert",
    "position": 100,
    "text": "Hello, World!",
    "length": 13,
    "clientId": "client_456"
  },
  "userId": "user_123",
  "username": "john_doe",
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "metadata": {
    "userAgent": "Mozilla/5.0...",
    "ipAddress": "192.168.1.100",
    "sessionId": "session_789"
  }
}
```

#### execution_logs - 代码执行日志集合
```javascript
// 集合索引
db.execution_logs.createIndexes([
  { "userId": 1, "timestamp": 1 },
  { "projectId": 1, "timestamp": 1 },
  { "language": 1, "timestamp": 1 },
  { "success": 1, "timestamp": 1 }
]);

// 文档结构
{
  "_id": ObjectId("..."),
  "executionId": "exec_1640995200_abc123",
  "userId": "user_123",
  "projectId": "proj_abc123",
  "language": "javascript",
  "code": "console.log('Hello, Codivio!');",
  "input": "",
  "result": {
    "success": true,
    "output": "Hello, Codivio!\n",
    "error": "",
    "exitCode": 0,
    "executionTime": 145,
    "memoryUsage": 12
  },
  "environment": {
    "dockerImage": "node:18-alpine",
    "timeout": 30,
    "memoryLimit": 128
  },
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "metadata": {
    "userAgent": "Mozilla/5.0...",
    "ipAddress": "192.168.1.100"
  }
}
```

#### system_logs - 系统日志集合
```javascript
// 按月分片的集合
// 集合命名: system_logs_YYYYMM
db.system_logs_202401.createIndexes([
  { "timestamp": 1 },
  { "level": 1, "timestamp": 1 },
  { "service": 1, "timestamp": 1 },
  { "userId": 1, "timestamp": 1 }
]);

// 文档结构
{
  "_id": ObjectId("..."),
  "level": "INFO",         // DEBUG, INFO, WARN, ERROR
  "service": "editor-service",
  "message": "User joined document collaboration",
  "userId": "user_123",
  "correlationId": "req_123456",
  "details": {
    "documentId": "doc_123",
    "projectId": "proj_abc123",
    "action": "join_document",
    "metadata": {
      "userAgent": "Mozilla/5.0...",
      "ipAddress": "192.168.1.100"
    }
  },
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "hostname": "codivio-editor-pod-abc123"
}
```

### 数据归档策略
```javascript
// 自动过期设置
db.operation_history.createIndex(
  { "timestamp": 1 }, 
  { expireAfterSeconds: 7776000 } // 90天后自动删除
);

db.execution_logs.createIndex(
  { "timestamp": 1 }, 
  { expireAfterSeconds: 2592000 } // 30天后自动删除
);

// 手动归档脚本
function archiveOldData() {
  const cutoffDate = new Date(Date.now() - 90 * 24 * 60 * 60 * 1000); // 90天前
  
  // 归档操作历史
  const operations = db.operation_history.find({
    "timestamp": { $lt: cutoffDate }
  });
  
  operations.forEach(doc => {
    db.operation_history_archive.insert(doc);
  });
  
  db.operation_history.deleteMany({
    "timestamp": { $lt: cutoffDate }
  });
}
```

## 🔍 查询优化

### MySQL查询优化

#### 常用查询示例
```sql
-- 获取用户的项目列表（分页）
SELECT p.*, pm.role, pm.joined_at,
       (SELECT COUNT(*) FROM project_members pm2 WHERE pm2.project_id = p.id) as member_count
FROM projects p
JOIN project_members pm ON p.id = pm.project_id
WHERE pm.user_id = ? 
  AND p.status = 1
  AND p.deleted_at IS NULL
ORDER BY p.last_activity_at DESC
LIMIT ?, ?;

-- 获取项目的提交历史
SELECT gc.*, u.username, u.avatar_url
FROM git_commits gc
LEFT JOIN codivio_user.users u ON gc.author_id = u.id
WHERE gc.project_id = ?
  AND gc.branch = ?
ORDER BY gc.commit_time DESC
LIMIT ?, ?;

-- 搜索项目（全文搜索）
SELECT p.*, pm.role,
       MATCH(p.name, p.description) AGAINST(? IN NATURAL LANGUAGE MODE) as relevance
FROM projects p
JOIN project_members pm ON p.id = pm.project_id
WHERE pm.user_id = ?
  AND (p.visibility = 'public' OR pm.role IN ('owner', 'admin', 'editor'))
  AND MATCH(p.name, p.description) AGAINST(? IN NATURAL LANGUAGE MODE)
ORDER BY relevance DESC, p.last_activity_at DESC
LIMIT ?, ?;
```

#### 性能优化配置
```sql
-- MySQL配置优化
SET GLOBAL innodb_buffer_pool_size = 2147483648; -- 2GB
SET GLOBAL innodb_log_file_size = 268435456;     -- 256MB
SET GLOBAL query_cache_size = 134217728;         -- 128MB
SET GLOBAL max_connections = 1000;
SET GLOBAL slow_query_log = 1;
SET GLOBAL long_query_time = 2;

-- 添加全文索引
ALTER TABLE projects ADD FULLTEXT(name, description);
ALTER TABLE users ADD FULLTEXT(username, display_name, bio);
```

### Redis性能优化
```yaml
# redis.conf优化配置
maxmemory: 2gb
maxmemory-policy: allkeys-lru
save: 900 1 300 10 60 10000  # RDB持久化
appendonly: yes               # AOF持久化
appendfsync: everysec        # AOF同步策略
tcp-keepalive: 300           # TCP keepalive
timeout: 300                 # 客户端超时时间
```

### MongoDB性能优化
```javascript
// 复合索引优化
db.operation_history.createIndex(
  { 
    "documentId": 1, 
    "timestamp": 1,
    "operation.type": 1 
  },
  { 
    name: "doc_time_type_idx",
    background: true 
  }
);

// 聚合查询优化
db.operation_history.aggregate([
  {
    $match: {
      "documentId": "doc_123",
      "timestamp": { 
        $gte: ISODate("2024-01-01T00:00:00Z"),
        $lt: ISODate("2024-02-01T00:00:00Z")
      }
    }
  },
  {
    $group: {
      "_id": {
        "userId": "$userId",
        "type": "$operation.type"
      },
      "count": { $sum: 1 }
    }
  },
  {
    $sort: { "count": -1 }
  }
]).hint("doc_time_type_idx");
```