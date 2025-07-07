# 系统架构设计

## 🏗️ 整体架构

### 架构概览
```
┌─────────────────────────────────────────────────────────────┐
│                        用户层                                │
├─────────────────┬─────────────────┬─────────────────────────┤
│   Web浏览器      │   移动端App      │   VSCode插件             │
│   Vue 3         │   UniApp        │   TypeScript            │
└─────────────────┴─────────────────┴─────────────────────────┘
                              │
                    ┌─────────────────┐
                    │   CDN / Nginx   │
                    │   负载均衡       │
                    └─────────────────┘
                              │
                    ┌─────────────────┐
                    │  API Gateway    │
                    │ Spring Gateway  │
                    └─────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   ┌─────────┐         ┌─────────┐         ┌─────────┐
   │ 用户服务  │         │ 项目服务  │         │ 编辑服务  │
   │ User    │         │ Project │         │ Editor  │
   │ Service │         │ Service │         │ Service │
   └─────────┘         └─────────┘         └─────────┘
        │                     │                     │
   ┌─────────┐         ┌─────────┐         ┌─────────┐
   │  MySQL  │         │  MySQL  │         │  Redis  │
   │ 用户数据  │         │ 项目数据  │         │ 实时状态  │
   └─────────┘         └─────────┘         └─────────┘
                              │
                    ┌─────────────────┐
                    │   执行环境服务    │
                    │  Docker Runner  │
                    └─────────────────┘
```

## 🎯 微服务设计

### 服务拆分策略

#### 1. 用户服务 (User Service)
**职责**: 用户认证、授权、个人信息管理
```yaml
端口: 8081
数据库: user_db (MySQL)
技术栈: Spring Boot + Spring Security + JWT
主要功能:
  - 用户注册/登录
  - JWT令牌管理
  - 权限控制
  - 个人资料管理
```

#### 2. 项目服务 (Project Service)
**职责**: 项目管理、文件管理、团队管理
```yaml
端口: 8082
数据库: project_db (MySQL)
技术栈: Spring Boot + MyBatis Plus + JGit
主要功能:
  - 项目CRUD操作
  - 文件系统管理
  - Git版本控制
  - 团队权限管理
```

#### 3. 编辑服务 (Editor Service)
**职责**: 实时协作编辑、操作转换、状态同步
```yaml
端口: 8083
数据库: Redis (实时状态) + MongoDB (操作历史)
技术栈: Spring Boot + WebSocket + Redis
主要功能:
  - 实时协作编辑
  - 操作转换算法
  - 光标位置同步
  - 在线用户管理
```

#### 4. 执行服务 (Execution Service)
**职责**: 代码执行、环境管理、安全隔离
```yaml
端口: 8084
技术栈: Spring Boot + Docker API
主要功能:
  - 多语言代码执行
  - Docker容器管理
  - 资源限制和监控
  - 安全沙箱
```

### 服务间通信

#### 同步通信 (REST API)
```java
// 服务发现配置
@EnableEurekaClient
@RestController
public class UserController {
    
    @Autowired
    private ProjectServiceClient projectServiceClient;
    
    @GetMapping("/user/{userId}/projects")
    public List<Project> getUserProjects(@PathVariable String userId) {
        return projectServiceClient.getProjectsByUserId(userId);
    }
}

// Feign客户端
@FeignClient("project-service")
public interface ProjectServiceClient {
    @GetMapping("/internal/projects/user/{userId}")
    List<Project> getProjectsByUserId(@PathVariable("userId") String userId);
}
```

#### 异步通信 (消息队列)
```yaml
# RabbitMQ消息配置
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    
# 消息主题设计
exchanges:
  - user.events      # 用户事件
  - project.events   # 项目事件
  - editor.events    # 编辑事件
  
queues:
  - user.created     # 用户创建事件
  - project.updated  # 项目更新事件
  - editor.operation # 编辑操作事件
```

## 💾 数据存储设计

### 数据库选型和分布

#### MySQL - 关系型数据
```sql
-- 用户数据库
CREATE DATABASE user_db;
USE user_db;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 项目数据库
CREATE DATABASE project_db;
USE project_db;

-- 项目表
CREATE TABLE projects (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    language VARCHAR(20) DEFAULT 'javascript',
    visibility ENUM('private', 'public') DEFAULT 'private',
    git_repository VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 项目成员表
CREATE TABLE project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('owner', 'admin', 'editor', 'viewer') DEFAULT 'editor',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_member (project_id, user_id)
);
```

#### Redis - 缓存和实时状态
```yaml
# Redis数据结构设计
databases:
  0: # 会话缓存
    - "session:{sessionId}" : "用户会话信息"
    - "user:{userId}:online" : "用户在线状态"
    
  1: # 编辑状态
    - "document:{docId}" : "文档内容和版本"
    - "room:{docId}:users" : "房间在线用户列表"
    - "operation:{docId}:queue" : "操作队列"
    
  2: # 系统缓存  
    - "project:{projectId}" : "项目信息缓存"
    - "user:{userId}:permissions" : "用户权限缓存"
```

#### MongoDB - 操作历史和日志
```javascript
// 操作历史集合
db.operation_history.createIndex({ "documentId": 1, "timestamp": 1 })
db.operation_history.insert({
  documentId: "doc_123",
  operation: {
    type: "insert",
    position: 100,
    text: "hello world",
    clientId: "client_456"
  },
  timestamp: new Date(),
  userId: "user_789"
})

// 系统日志集合
db.system_logs.createIndex({ "timestamp": 1, "level": 1 })
db.system_logs.insert({
  level: "INFO",
  service: "editor-service",
  message: "User joined document",
  metadata: {
    userId: "user_123",
    documentId: "doc_456"
  },
  timestamp: new Date()
})
```

## 🔄 实时通信架构

### WebSocket连接管理
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CollaborativeEditHandler(), "/ws/editor")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

// 连接池管理
@Component
public class WebSocketConnectionManager {
    private final Map<String, Set<WebSocketSession>> documentSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToDocument = new ConcurrentHashMap<>();
    
    public void addSession(String documentId, WebSocketSession session) {
        documentSessions.computeIfAbsent(documentId, k -> new ConcurrentHashMap<>())
                        .add(session);
        sessionToDocument.put(session.getId(), documentId);
    }
    
    public void removeSession(WebSocketSession session) {
        String documentId = sessionToDocument.remove(session.getId());
        if (documentId != null) {
            Set<WebSocketSession> sessions = documentSessions.get(documentId);
            if (sessions != null) {
                sessions.remove(session);
            }
        }
    }
}
```

### 操作转换算法核心
```java
@Service
public class OperationTransformService {
    
    public Operation transformOperation(Operation op1, Operation op2) {
        // 插入-插入转换
        if (op1.isInsert() && op2.isInsert()) {
            if (op1.getPosition() <= op2.getPosition()) {
                return op2.withPosition(op2.getPosition() + op1.getLength());
            }
            return op2;
        }
        
        // 删除-删除转换
        if (op1.isDelete() && op2.isDelete()) {
            if (op1.getPosition() + op1.getLength() <= op2.getPosition()) {
                return op2.withPosition(op2.getPosition() - op1.getLength());
            }
            if (op1.getPosition() >= op2.getPosition() + op2.getLength()) {
                return op2;
            }
            // 重叠删除处理
            return handleOverlappingDelete(op1, op2);
        }
        
        // 插入-删除转换
        if (op1.isInsert() && op2.isDelete()) {
            if (op1.getPosition() <= op2.getPosition()) {
                return op2.withPosition(op2.getPosition() + op1.getLength());
            }
            if (op1.getPosition() < op2.getPosition() + op2.getLength()) {
                return op2.withLength(op2.getLength() + op1.getLength());
            }
            return op2;
        }
        
        return op2;
    }
}
```

## 🚀 部署架构

### Kubernetes部署
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: codivio

---
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: codivio-config
  namespace: codivio
data:
  mysql.url: "jdbc:mysql://mysql-service:3306/codivio"
  redis.host: "redis-service"
  
---
# k8s/user-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: codivio
spec:
  replicas: 2
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: codivio/user-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: MYSQL_URL
          valueFrom:
            configMapKeyRef:
              name: codivio-config
              key: mysql.url
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: codivio
spec:
  selector:
    app: user-service
  ports:
  - port: 8081
    targetPort: 8081
```

### 监控和观测

#### Prometheus监控配置
```yaml
# prometheus/prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'codivio-services'
    static_configs:
      - targets: ['user-service:8081', 'project-service:8082', 'editor-service:8083']
    metrics_path: '/actuator/prometheus'
    
rule_files:
  - "codivio-rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']
```

#### 应用性能监控
```java
// 自定义指标
@RestController
public class MetricsController {
    
    private final MeterRegistry meterRegistry;
    private final Counter collaborationCounter;
    
    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.collaborationCounter = Counter.builder("codivio.collaboration.operations")
                .description("Number of collaboration operations")
                .tag("service", "editor")
                .register(meterRegistry);
    }
    
    @EventListener
    public void handleCollaborationOperation(CollaborationOperationEvent event) {
        collaborationCounter.increment(
            Tags.of("operation", event.getOperationType(),
                   "document", event.getDocumentId())
        );
    }
}
```

## 🔐 安全架构

### 安全层级设计
```
┌─────────────────────────────────────┐
│           网络安全层                 │
│  - HTTPS/WSS加密传输                │
│  - API网关限流和防护                │
│  - WAF Web应用防火墙                │
└─────────────────────────────────────┘
                   │
┌─────────────────────────────────────┐
│           认证授权层                 │
│  - JWT令牌认证                      │
│  - OAuth2.0第三方登录               │
│  - 细粒度权限控制                   │
└─────────────────────────────────────┘
                   │
┌─────────────────────────────────────┐
│           应用安全层                 │
│  - 输入验证和过滤                   │
│  - SQL注入防护                      │
│  - XSS攻击防护                      │
└─────────────────────────────────────┘
                   │
┌─────────────────────────────────────┐
│           数据安全层                 │
│  - 数据加密存储                     │
│  - 敏感信息脱敏                     │
│  - 定期数据备份                     │
└─────────────────────────────────────┘
                   │
┌─────────────────────────────────────┐
│           基础设施安全层              │
│  - Docker容器隔离                   │
│  - 网络策略限制                     │
│  - 系统安全加固                     │
└─────────────────────────────────────┘
```

### 代码执行安全
```java
@Service
public class SecureCodeExecutor {
    
    @Value("${security.execution.timeout:30}")
    private int executionTimeout;
    
    @Value("${security.execution.memory:128}")
    private int memoryLimit;
    
    public ExecutionResult executeCode(String code, String language) {
        // 安全检查
        validateCode(code, language);
        
        // 创建安全容器
        ContainerConfig config = ContainerConfig.builder()
            .image(getSecureImage(language))
            .cmd(Arrays.asList("timeout", String.valueOf(executionTimeout) + "s", 
                              getExecutionCommand(language)))
            .workingDir("/sandbox")
            .user("sandbox:sandbox") // 非root用户执行
            .build();
            
        HostConfig hostConfig = HostConfig.builder()
            .memory((long) memoryLimit * 1024 * 1024)
            .cpuShares(512L)
            .networkMode("none") // 禁用网络访问
            .readonlyRootfs(true) // 只读根文件系统
            .noNewPrivileges(true) // 禁止提权
            .build();
            
        return executeInContainer(config, hostConfig, code);
    }
    
    private void validateCode(String code, String language) {
        // 恶意代码检测
        List<String> dangerousPatterns = Arrays.asList(
            "import\\s+os", "exec\\s*\\(", "eval\\s*\\(",
            "Runtime\\.getRuntime", "ProcessBuilder",
            "File\\s*\\(", "FileWriter", "socket"
        );
        
        for (String pattern : dangerousPatterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(code).find()) {
                throw new SecurityException("检测到潜在的危险代码: " + pattern);
            }
        }
    }
}
```