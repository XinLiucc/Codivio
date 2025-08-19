# Codivio 开发待办清单

> 基于当前框架评估生成的开发任务清单，按优先级排序

## 🚨 优先级1: 立即处理 (本周必须完成)

### 1.1 数据层基础建设 ✅
- [x] **配置数据库连接** ✅ 2025-08-18 完成
  - [x] 添加MySQL JDBC依赖到用户微服务
  - [x] 配置Spring Data JPA
  - [x] 设置数据库连接池(HikariCP)
  - [x] 配置多环境数据库连接(dev/docker)

- [x] **用户服务数据层** ✅ 2025-08-18 完成
  - [x] 设计用户表结构(users表已完成)
  - [x] 创建User实体类(11个字段，完整JPA注解)
  - [x] 实现UserRepository接口(7个查询方法)
  - [x] 添加数据库初始化脚本(已存在)
  - [x] 配置JPA自动建表策略(validate模式)

- [x] **基础用户API实现** ✅ 2025-08-19 完成
  - [x] 实现用户注册API (POST /api/v1/auth/register) ✅
  - [x] 实现用户登录API (POST /api/v1/auth/login) ✅ 2025-08-19 晚间
  - [x] 添加统一响应格式封装 (ResultVO) ✅
  - [x] 实现用户名邮箱检查API ✅
  - [ ] 实现用户信息查询API (GET /api/v1/users/profile)
  - [ ] 实现用户信息更新API (PUT /api/v1/users/profile)

### 1.2 基础认证机制 ✅ 
- [x] **JWT认证实现** ✅ 2025-08-18 完成
  - [x] 添加JWT依赖(jjwt-api, jjwt-impl, jjwt-jackson)
  - [x] 实现JwtUtil工具类(5个核心方法)
  - [x] 创建JWT配置类(secret/expiration/issuer)
  - [x] 实现Token生成和验证逻辑(完整异常处理)

- [ ] **Spring Security配置** 🔄 部分完成
  - [x] 添加Spring Security依赖(spring-security-crypto) ✅
  - [x] 配置密码加密(BCryptPasswordEncoder) ✅
  - [ ] 配置SecurityFilterChain
  - [ ] 实现JwtAuthenticationFilter
  - [ ] 设置CORS配置

## ⚡ 优先级2: 本周内完成

### 2.1 API网关完善
- [ ] **Gateway路由优化**
  - [ ] 完善网关路由配置
  - [ ] 添加负载均衡配置
  - [ ] 实现Gateway全局过滤器
  - [ ] 添加请求日志记录

- [ ] **统一认证中心**
  - [ ] 在Gateway实现统一JWT验证
  - [ ] 配置白名单路径
  - [ ] 实现用户信息传递到下游服务

### 2.2 项目服务基础
- [ ] **项目数据模型**
  - [ ] 设计项目表结构(projects, project_members)
  - [ ] 创建Project、ProjectMember实体类
  - [ ] 实现ProjectRepository接口

- [ ] **项目基础API**
  - [ ] 实现项目创建API (POST /api/v1/projects)
  - [ ] 实现项目列表API (GET /api/v1/projects)
  - [ ] 实现项目详情API (GET /api/v1/projects/{id})
  - [ ] 实现项目成员管理API

### 2.3 异常处理和验证
- [ ] **统一异常处理** 🔄 部分完成  
  - [x] 统一错误响应格式(ResultVO) ✅
  - [x] 添加参数验证注解(Bean Validation) ✅
  - [ ] 创建全局异常处理器
  - [ ] 定义业务异常类

## 📋 优先级3: 下周完成

### 3.1 服务间通信
- [ ] **服务发现机制**
  - [ ] 评估是否需要Eureka/Consul
  - [ ] 或者使用Docker内部DNS
  - [ ] 配置服务间调用

- [ ] **服务间调用实现**
  - [ ] 添加OpenFeign依赖
  - [ ] 实现用户服务Feign客户端
  - [ ] 在项目服务中调用用户服务验证用户信息

### 3.2 文件服务实现
- [ ] **文件存储基础**
  - [ ] 配置本地文件存储路径
  - [ ] 实现文件上传API
  - [ ] 实现文件下载API
  - [ ] 添加文件类型和大小限制

### 3.3 协作服务基础
- [ ] **实时通信准备**
  - [ ] 添加WebSocket依赖
  - [ ] 设计消息表结构
  - [ ] 实现基础消息API

## 🔧 优先级4: 后续优化

### 4.1 监控和日志
- [ ] **日志体系完善**
  - [ ] 统一日志格式
  - [ ] 添加链路追踪ID
  - [ ] 配置日志输出到文件

- [ ] **健康检查增强**
  - [ ] 添加数据库连接检查
  - [ ] 添加Redis连接检查
  - [ ] 实现自定义健康指标

### 4.2 前端基础功能
- [ ] **用户界面实现**
  - [ ] 实现登录注册页面
  - [ ] 实现用户dashboard
  - [ ] 集成API调用
  - [ ] 添加Token管理

### 4.3 配置管理
- [ ] **环境配置优化**
  - [ ] 分离开发/测试/生产配置
  - [ ] 使用Spring Profiles
  - [ ] 敏感信息环境变量化

### 4.4 测试体系
- [ ] **单元测试**
  - [ ] 为Repository层添加测试
  - [ ] 为Service层添加测试
  - [ ] 为Controller层添加测试

- [ ] **集成测试**
  - [ ] 添加TestContainers
  - [ ] 实现API集成测试
  - [ ] 数据库集成测试

## 📊 进度跟踪

### 当前状态  
- ✅ Docker环境搭建完成
- ✅ 基础项目结构完成
- ✅ 数据层实现: 100% (User实体+Repository完成)
- ✅ 认证系统: 85% (JWT工具+密码加密+登录验证完成，缺SecurityFilter)
- ✅ 业务API: 80% (注册+登录API完成，缺查询/更新API)

### 里程碑目标
- **第1周**: 完成用户服务基础功能(注册/登录/JWT)
- **第2周**: 完成项目服务基础功能和网关认证
- **第3周**: 完成服务间通信和文件服务
- **第4周**: 前端基础功能集成

## 🎯 注意事项

1. **开发原则**: 先完成核心功能，再考虑优化
2. **测试优先**: 每个API都要先测试再继续
3. **文档同步**: 重要功能实现后及时更新文档
4. **Git提交**: 保持小颗粒度提交，便于回滚
5. **代码质量**: 遵循Spring Boot最佳实践

---

*最后更新: 2025-08-19*  
*下次评估: 完成用户登录API和Spring Security配置后*

## 🎉 最新完成功能 (2025-08-19)

### 用户注册系统 ✅ (白天完成)
- **ResultVO统一响应格式**: 泛型支持，统一success/error处理
- **UserRegisterDTO请求验证**: Bean Validation注解完整覆盖  
- **SecurityConfig密码加密**: BCrypt加密配置
- **UserService业务逻辑**: 完整的注册流程+验证逻辑
- **UserController REST API**: 3个端点(注册/用户名检查/邮箱检查)
- **Maven配置优化**: 解决Jackson冲突+参数名保留
- **JPA配置调整**: ddl-auto改为update模式

### 用户登录系统 ✅ (晚间完成)
- **UserLoginDTO登录请求**: 灵活的loginId字段(支持用户名/邮箱登录)
- **LoginResponseDTO登录响应**: 包含JWT token、用户信息、过期时间
- **登录业务逻辑**: findByUsernameOrEmail OR查询优化、BCrypt密码验证
- **JWT工具增强**: 添加getExpiration()方法支持过期时间返回
- **登录API接口**: POST /api/v1/auth/login，完整的验证和异常处理
- **安全处理**: 敏感信息过滤、readOnly事务优化

### 功能测试验证 ✅
- **测试账号**: testuser / test@example.com / password123
- **登录测试**: 用户名登录 ✅、邮箱登录 ✅、错误处理 ✅
- **JWT Token**: 24小时有效期，完整的用户信息返回

### 技术问题解决记录 ✅
1. **Jackson依赖冲突** → 添加显式jackson-databind依赖
2. **JPA验证错误** → ddl-auto从validate改为update  
3. **@PathVariable参数识别** → maven-compiler-plugin配置parameters=true
4. **Bean Validation引用错误** → 添加spring-boot-starter-validation依赖
5. **JwtUtil缺少方法** → 添加getExpiration()方法

### 下个阶段重点 🎯
1. **Spring Security完整配置**: SecurityFilterChain + JwtAuthenticationFilter
2. **用户信息管理**: 查询和更新API实现  
3. **全局异常处理**: @ControllerAdvice统一异常处理器