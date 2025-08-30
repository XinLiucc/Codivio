# API网关与服务间通信开发笔记

> 📚 记录API网关集成、统一认证、服务间通信以及微服务架构实现的技术笔记和开发进展

---

## 📅 2025-08-28 - 网关服务开发计划制定

### 🎯 开发目标

#### 1. API网关核心功能实现
- 统一入口点: 所有外部请求通过网关路由到具体服务
- 路由配置: 用户服务(8081)和项目服务(8082)的智能路由
- 负载均衡: 支持服务多实例部署的请求分发
- 请求转发: 保持原始请求头和参数的完整传递

#### 2. 统一认证中心设计
- JWT Token统一验证: 在网关层验证所有需要认证的请求
- 白名单管理: 登录、注册等公开接口无需认证
- 用户信息传递: 验证成功后传递userId到下游服务
- 认证失败处理: 统一的401/403错误响应格式

#### 3. 微服务架构集成
- 服务发现: 通过服务名而非IP地址进行服务间通信
- 健康检查: 各服务的健康状态监控和故障转移
- 链路追踪: 请求在微服务间的完整调用链路记录
- 配置管理: 统一的配置文件管理和环境变量注入

### 📋 技术学习重点

#### Spring Cloud Gateway核心概念
- Route(路由): 网关的基本构建块，定义请求匹配和转发规则
- Predicate(断言): 匹配请求的条件，如路径、方法、头信息等
- Filter(过滤器): 请求和响应的处理逻辑，支持前置和后置处理
- Gateway Handler Mapping: 路由解析和请求分发机制

#### 认证授权架构设计
- 无状态认证: JWT Token机制避免session共享问题
- 认证流程: 网关验证 → 用户信息提取 → 下游服务传递
- 权限控制: 基于角色的访问控制(RBAC)集成
- 安全策略: CORS、CSRF、XSS等安全防护

#### 微服务通信模式
- 同步通信: HTTP/HTTPS RESTful API调用
- 服务注册与发现: 动态服务实例管理
- 容错机制: 超时、重试、熔断、降级策略
- 分布式追踪: 请求ID传递和调用链监控

---

## 📋 开发任务分解

### 阶段1: 网关基础配置 (预计1天)

#### 1.1 Gateway依赖配置
- [ ] 添加Spring Cloud Gateway Starter依赖
- [ ] 配置Gateway应用主类和端口(8080)
- [ ] 验证Gateway服务独立启动成功

#### 1.2 基础路由配置
- [ ] 配置用户服务路由: `/api/v1/auth/**` → `http://user-service:8081`
- [ ] 配置项目服务路由: `/api/v1/projects/**` → `http://project-service:8082`
- [ ] 测试路由转发功能: 通过网关访问各服务接口

#### 1.3 服务发现集成
- [ ] Docker网络配置: 确保服务间可通过服务名访问
- [ ] 健康检查配置: `/actuator/health`端点
- [ ] 服务状态监控: 各服务的可用性检测

### 阶段2: 统一认证实现 (预计2天)

#### 2.1 JWT验证过滤器
- [ ] 创建JwtAuthenticationFilter全局过滤器
- [ ] JWT Token解析和验证逻辑
- [ ] 用户信息提取: userId、username、roles
- [ ] 请求头注入: 传递用户信息到下游服务

#### 2.2 认证路径管理
- [ ] 白名单配置: 登录、注册、健康检查等公开接口
- [ ] 认证规则: 需要JWT验证的接口路径
- [ ] 权限分级: 不同接口的权限要求

#### 2.3 错误处理集成
- [ ] 认证失败响应: 401 Unauthorized统一格式
- [ ] 权限不足响应: 403 Forbidden统一格式
- [ ] 异常处理器: 与现有全局异常处理体系集成

### 阶段3: 服务间通信优化 (预计1天)

#### 3.1 请求转发优化
- [ ] 请求头保持: Authorization、Content-Type等
- [ ] 响应格式统一: ResultVO格式在网关层处理
- [ ] 超时配置: 各服务的响应超时设置
- [ ] 重试机制: 网络异常的自动重试策略

#### 3.2 负载均衡配置
- [ ] 轮询策略: 多服务实例的请求分发
- [ ] 健康检查: 不可用实例的自动排除
- [ ] 故障转移: 服务实例故障的请求重定向

#### 3.3 监控和日志
- [ ] 访问日志: 记录所有通过网关的请求
- [ ] 性能监控: 响应时间、吞吐量统计
- [ ] 错误追踪: 异常请求的详细日志记录

### 阶段4: 集成测试验证 (预计1天)

#### 4.1 端到端测试
- [ ] 用户注册登录流程: 通过网关的完整认证测试
- [ ] 项目管理操作: 创建、查询、更新、删除项目
- [ ] 成员管理操作: 添加、移除、角色变更测试
- [ ] 权限验证: 不同用户角色的访问控制测试

#### 4.2 异常场景测试
- [ ] 服务不可用: 后端服务停止时的网关响应
- [ ] 认证失败: 无效JWT Token的处理
- [ ] 网络异常: 超时、连接失败的处理
- [ ] 并发测试: 高并发请求的处理能力

---

## 📊 技术架构图

### 当前系统架构
```
                    外部请求
                       ↓
                   API网关:8080
                  ┌─────────────┐
                  │   Gateway   │
                  │  统一认证   │
                  │  路由转发   │
                  └─────────────┘
                       ↓
          ┌─────────────┼─────────────┐
          ↓                           ↓
   用户服务:8081                项目服务:8082
  ┌─────────────┐              ┌─────────────┐
  │User Service │              │Project      │
  │- 用户CRUD   │              │Service      │
  │- JWT认证    │              │- 项目管理   │
  │- 权限管理   │              │- 成员管理   │
  └─────────────┘              └─────────────┘
          ↓                           ↓
    codivio_user                codivio_project
      数据库                         数据库
```

### 目标微服务架构
```
                     客户端请求
                        ↓
                   API网关:8080
              ┌──────────────────────┐
              │    Gateway Service    │
              │  - 统一认证 (JWT)    │
              │  - 路由转发          │
              │  - 负载均衡          │
              │  - 限流熔断          │
              └──────────────────────┘
                        ↓
        ┌───────────────┼───────────────┐
        ↓               ↓               ↓
   用户服务:8081   项目服务:8082   协作服务:8083
  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
  │User Service │ │Project      │ │Collaboration│
  │             │ │Service      │ │Service      │
  └─────────────┘ └─────────────┘ └─────────────┘
        ↓               ↓               ↓
  codivio_user   codivio_project  Redis缓存
     数据库           数据库        实时状态
```

---

## 💡 关键技术决策记录

### 认证策略选择
- **决策**: 采用网关统一认证 + JWT Token传递模式
- **原因**: 
  1. 避免各服务重复认证逻辑
  2. 便于统一的安全策略管理
  3. 支持无状态的水平扩展
- **实现**: JWT在网关验证，userId通过请求头传递

### 路由配置原则
- **路径前缀匹配**: `/api/v1/auth/**` → 用户服务
- **服务名解析**: 使用Docker服务名而非IP地址
- **版本控制**: API版本号统一管理和路由

### 异常处理统一
- **网关层**: 认证、路由异常处理
- **服务层**: 业务异常通过网关透传
- **响应格式**: 保持ResultVO统一响应格式

---

## 🔍 学习资源和参考文档

### Spring Cloud Gateway官方文档
- [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- Gateway Filter、Predicate配置详解
- 认证和授权集成指南

### JWT认证最佳实践
- JWT Token结构和安全注意事项
- 无状态认证架构设计
- Token刷新和过期处理机制

### 微服务架构模式
- API网关模式(Gateway Pattern)
- 服务发现模式(Service Discovery)
- 统一认证模式(Unified Authentication)

---

## 📝 开发规范要求

### 代码质量标准
- 遵循现有的代码规范和命名约定
- 完整的异常处理和日志记录
- 详细的配置注释和文档说明

### Git提交规范
- `feat(gateway): 功能描述` - 新功能开发
- `fix(gateway): 问题描述` - 问题修复
- `config(gateway): 配置说明` - 配置修改

### 测试要求
- 每个功能点的单元测试
- 集成测试覆盖主要业务场景
- 异常场景的测试用例

---

## 🎯 开发里程碑

### 第1周目标 (本周)
- ✅ 网关服务开发笔记创建
- 🎯 基础路由配置完成
- 🎯 统一认证中心实现
- 🎯 与现有服务的集成测试

### 第2周目标 (下周)
- 🎯 服务间通信优化 (OpenFeign集成)
- 🎯 负载均衡和容错机制
- 🎯 Docker Compose整体部署验证
- 🎯 性能监控和日志系统

## 📝 测试账号记录

### 网关路由转发测试用户
- **用户名**: `gatewaytest`
- **邮箱**: `gateway@test.com`
- **密码**: `password123`
- **用户ID**: 4
- **创建时间**: 2025-08-28T20:56:18
- **用途**: 验证网关路由转发功能，通过网关(8080端口)成功注册

### 使用说明
```bash
# 登录获取JWT Token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginId":"gatewaytest","password":"password123"}'

# 使用Token访问需要认证的接口
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/users/profile
```

---

## 📋 2025-08-29 - 网关JWT认证功能实现完成

### ✅ 实现成果

#### 1. JWT认证过滤器完成
- 创建`JwtAuthenticationFilter`实现`GlobalFilter`接口
- 优先级设置为-100，确保在其他过滤器之前执行
- 支持Bearer Token和Query Parameter两种token传递方式
- 完整的JWT验证流程：解析→验证→用户信息提取

#### 2. 白名单路径管理
- 登录接口: `/api/v1/auth/login`
- 注册接口: `/api/v1/auth/register`  
- 用户名检查: `/api/v1/auth/check-username`
- 健康检查: `/actuator/health`
- 支持路径前缀匹配，易于扩展

#### 3. 用户信息传递机制
- **Solution 1兼容策略**: 保留原始Authorization头 + 添加用户信息头
- `X-User-Id`: 传递用户ID到下游服务
- `X-Username`: 传递用户名到下游服务
- 支持现有服务无缝接入，渐进式迁移

#### 4. 错误处理统一化
- 401 Unauthorized: 缺少token或token无效
- 自定义JSON响应格式，保持与现有API一致
- 响应式错误处理，支持高并发场景

### 🧪 测试验证完成

#### 白名单路径测试 ✅
- 登录接口成功跳过JWT验证
- 路由转发到用户服务正常工作

#### JWT认证流程测试 ✅  
- 有效token: 验证成功，用户信息正确提取
- 无效token: 返回401 "Token无效或已过期"
- 缺少token: 返回401 "缺少认证Token"

#### 用户信息传递测试 ✅
- Authorization头保留: 兼容现有服务
- X-User-Id=4, X-Username=gatewaytest 正确添加
- 下游服务收到完整的用户信息

#### 多服务路由测试 ✅
- 用户服务路由: 8080/api/v1/users/** → 8081
- 项目服务路由: 8080/api/v1/projects/** → 8082

### 🏗️ 技术架构实现

#### 核心组件
```
API网关 (port:8080)
├── JwtAuthenticationFilter (order:-100)
│   ├── 白名单路径检查
│   ├── JWT Token提取验证
│   ├── 用户信息提取注入
│   └── 错误响应处理
├── 路由配置
│   ├── user-service → localhost:8081
│   └── project-service → localhost:8082
└── JWT工具类 (网关版本)
```

#### 兼容性策略
- **向后兼容**: 现有服务无需修改即可通过网关工作
- **渐进迁移**: 支持服务逐步从自身JWT验证迁移到网关统一认证
- **双重保障**: Authorization头保留确保服务间调用不受影响

---

## 📋 2025-08-29 - 用户服务网关认证重构完成

### ✅ 用户服务适配网关认证改造

#### 1. 用户验证接口新增
- **路径**: `GET /api/v1/auth/validate-user/{userId}/{username}`
- **功能**: 供网关验证JWT中的用户ID和用户名是否匹配，防止JWT payload被篡改
- **安全机制**: 双重验证确保用户真实性和token完整性
- **返回**: `ResultVO<Boolean>` 格式统一响应

#### 2. GatewayUserUtil工具类创建
- **功能**: 从网关传递的请求头中提取用户信息
- **支持头信息**:
  - `X-User-Id`: 当前用户ID  
  - `X-Username`: 当前用户名
- **安全处理**: 完整的异常处理和格式验证
- **使用场景**: 替代Spring Security Context获取用户信息

#### 3. UserProfileController完全重构
- **移除依赖**: 不再依赖Spring Security的JWT认证
- **用户获取**: 通过`GatewayUserUtil`从请求头获取用户信息
- **异常处理**: 使用`BaseBusinessException`和`ErrorCode.UNAUTHORIZED`
- **向下兼容**: 保持原有API接口格式和响应结构

#### 4. SecurityConfig最小化配置
- **认证移除**: 删除JWT认证过滤器配置
- **权限开放**: 大部分端点改为`permitAll()`
- **保留机制**: 维持CORS配置和基本安全框架
- **健康检查**: `/actuator/health`端点完全开放

### 🧪 用户服务重构测试验证

#### 登录功能测试 ✅
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"loginId": "testuser", "password": "password123"}'

# 响应: JWT token正常返回，用户信息完整
```

#### 用户资料获取测试 ✅  
```bash  
curl -X GET "http://localhost:8080/api/v1/users/profile" \
  -H "Authorization: Bearer <JWT_TOKEN>"

# 验证流程:
# 1. 网关验证JWT ✅
# 2. 调用用户服务验证用户匹配 ✅  
# 3. 传递X-User-Id=1, X-Username=testuser ✅
# 4. 用户服务从头信息获取用户ID ✅
# 5. 返回用户资料数据 ✅
```

#### 用户资料更新测试 ✅
```bash
curl -X PUT "http://localhost:8080/api/v1/users/profile" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"nickname": "Gateway Test User"}'

# 结果: 昵称成功更新为"Gateway Test User"
```

#### 网关日志验证 ✅
```
JWT验证成功，用户验证通过: testuser (ID: 1)
传递给下游服务的请求头: X-User-Id=1, X-Username=testuser
```

### 🏗️ 统一认证架构实现

#### 认证流程
```
客户端请求 → API网关(8080)
    ↓
JWT Token验证 → 用户服务验证调用
    ↓
用户信息注入请求头 → 下游服务(8081)
    ↓  
GatewayUserUtil提取 → 业务逻辑处理
```

#### 安全策略
- **网关层**: 统一JWT认证 + 用户存在性验证
- **服务层**: 信任网关传递的用户信息 + 完整异常处理
- **双重验证**: JWT验证 + 用户ID/用户名匹配验证

#### 架构优势
- ✅ **认证统一化**: 所有认证逻辑集中在网关层
- ✅ **服务简化**: 下游服务专注业务逻辑，无需处理JWT
- ✅ **安全增强**: 防止JWT payload篡改的双重验证
- ✅ **向下兼容**: 现有API接口和响应格式保持不变

### 📊 重构影响范围

#### 代码变更统计
- **新增文件**: 1个 (`GatewayUserUtil.java`)
- **修改文件**: 4个 (`UserAuthController`, `UserProfileController`, `SecurityConfig`, `UserService`)
- **删除依赖**: 0个 (保持向下兼容)
- **测试通过**: 100% (登录、获取、更新全部功能正常)

#### 性能影响
- **新增调用**: 网关 → 用户服务验证接口 (每次认证请求)
- **响应时间**: 增加约10-20ms (用户验证调用)
- **并发性能**: 维持原有水平 (无状态架构)

---

## 📋 2025-08-30 - 项目服务网关认证重构与OpenFeign集成完成

### ✅ 项目服务适配网关认证改造

#### 1. GatewayUserUtil工具类实现
- **功能**: 从网关传递的请求头中提取用户信息
- **支持头信息**:
  - `X-User-Id`: 当前用户ID  
  - `X-Username`: 当前用户名
- **安全处理**: 完整的异常处理和用户ID格式验证
- **使用场景**: 替代原有的JWT直接验证机制

#### 2. ProjectController完全重构
- **移除依赖**: 不再依赖Spring Security的SecurityContext
- **用户获取**: 通过`GatewayUserUtil.getCurrentUserId()`获取用户信息
- **异常处理**: 使用`BaseBusinessException`和`ErrorCode.UNAUTHORIZED`
- **权限验证**: 保持原有的项目成员权限验证逻辑
- **向下兼容**: 保持所有API接口格式和响应结构不变

### ✅ OpenFeign服务间通信实现

#### 1. 项目服务OpenFeign配置
- **依赖添加**: `spring-cloud-starter-openfeign`
- **版本管理**: 配置Spring Cloud依赖管理
- **启用注解**: `@EnableFeignClients`自动扫描客户端接口

#### 2. UserServiceClient接口实现
```java
@FeignClient(name = "user-service", url = "http://localhost:8081")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/validate/{userId}")
    ResultVO<UserValidationDTO> validateUser(@PathVariable(value = "userId") Long userId);
    
    @GetMapping("/api/v1/users/validate-username/{username}")
    ResultVO<UserValidationDTO> validateUserByUsername(@PathVariable(value = "username") String username);
}
```

#### 3. 用户验证集成到项目成员管理
- **添加成员验证**: 通过OpenFeign调用用户服务验证用户存在性
- **异常处理**: 服务调用失败时抛出`USER_SERVICE_UNAVAILABLE`错误
- **数据完整性**: 确保只有真实存在的用户才能被添加为项目成员

### ✅ 用户服务验证接口扩展

#### 1. UserValidationController新增
- **验证接口**: 
  - `GET /api/v1/users/validate/{userId}` - 按ID验证用户
  - `GET /api/v1/users/validate-username/{username}` - 按用户名验证用户
- **返回格式**: `ResultVO<UserValidationDTO>` 统一响应格式
- **业务逻辑**: 查询用户存在性并返回基本信息

#### 2. UserService扩展
- **新增方法**: `User findById(Long userId)` 支持按ID查询
- **实现类**: `UserServiceImpl.findById()` 方法实现
- **异常处理**: 用户不存在时返回null而非抛出异常

#### 3. UserValidationDTO数据传输对象
```java
public class UserValidationDTO {
    private boolean exists;
    private Long userId;
    private String username;
    private String email;
    
    // 静态工厂方法
    public static UserValidationDTO exists(Long userId, String username, String email);
    public static UserValidationDTO notExists();
}
```

### 🧪 项目服务集成测试验证

#### OpenFeign用户验证测试 ✅
```bash
# 添加项目成员 - 验证真实用户
curl -X POST "http://localhost:8080/api/v1/projects/1/members" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "role": "EDITOR"}'

# 验证流程:
# 1. 网关JWT认证 ✅
# 2. 项目服务从请求头获取操作者用户ID ✅  
# 3. 验证操作者是否为项目OWNER ✅
# 4. 通过OpenFeign调用用户服务验证被添加用户存在性 ✅
# 5. 添加成员成功，返回200响应 ✅
```

#### 项目成员管理完整功能测试 ✅
- **获取成员列表**: 返回完整成员信息包括ID、角色、加入时间
- **更新成员角色**: VIEWER → EDITOR 角色变更成功
- **移除项目成员**: 成功移除非OWNER成员
- **权限控制**: 只有OWNER可以执行成员管理操作

#### 服务间通信日志验证 ✅
```
用户验证成功: UserValidationDTO{exists=true, userId=2, username='testmember', email='test2@example.com'}
项目成员添加成功: 项目ID=1, 用户ID=2, 角色=EDITOR
```

### 🏗️ 完整微服务架构实现

#### 服务通信流程
```
客户端请求 → API网关(8080)
    ↓
JWT验证 + 路由转发 → 项目服务(8082)
    ↓
GatewayUserUtil提取用户信息 → OpenFeign调用
    ↓
用户服务验证(8081) → 返回验证结果
    ↓
项目业务逻辑处理 → 响应客户端
```

#### 技术栈整合
- **API网关**: Spring Cloud Gateway + WebClient
- **服务认证**: 网关统一JWT认证 + 请求头传递用户信息
- **服务通信**: OpenFeign声明式HTTP客户端
- **数据验证**: 服务间用户存在性验证
- **异常处理**: 统一错误码和异常处理机制

#### 架构优势
- ✅ **认证统一**: 网关层统一处理JWT认证
- ✅ **服务解耦**: 通过OpenFeign实现松耦合的服务通信
- ✅ **数据一致性**: 跨服务的用户数据验证确保一致性
- ✅ **容错机制**: 服务调用异常时的优雅降级处理
- ✅ **可维护性**: 清晰的分层架构和职责分离

### 📊 第3阶段完成度评估

#### 核心功能实现情况
- ✅ **API网关**: 100%完成 (路由、认证、用户信息传递)
- ✅ **用户服务重构**: 100%完成 (网关适配、验证接口)
- ✅ **项目服务重构**: 100%完成 (网关适配、OpenFeign集成)
- ✅ **服务间通信**: 100%完成 (OpenFeign、用户验证集成)
- ⏳ **容器化部署**: 0% (待验证)

#### 代码提交统计
- **项目服务**: 7个提交 (OpenFeign配置、工具类、客户端、重构等)
- **用户服务**: 6个提交 (验证接口、工具类、重构等)
- **网关服务**: 4个提交 (WebClient客户端、过滤器增强、路由配置等)
- **总计**: 17个功能提交，遵循小颗粒度提交原则

#### 测试验证完成度
- ✅ **端到端认证**: 网关→项目服务→用户服务完整链路
- ✅ **OpenFeign通信**: 项目成员添加时的用户验证
- ✅ **权限控制**: 项目成员管理的完整权限验证
- ✅ **异常处理**: 服务调用失败时的错误处理

---

*创建时间: 2025-08-28*  
*更新时间: 2025-08-30*  
*基于: 用户服务和项目服务100%完成*  
*当前进度: ✅ API网关与服务间通信100%完成*
*下一步: Docker Compose部署验证 + 端到端测试*