backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── codivio/
│   │   │           ├── CodivioApplication.java           # 主启动类
│   │   │           ├── common/                           # 通用组件
│   │   │           │   └── Result.java                   # 统一响应结果
│   │   │           ├── config/                           # 配置类
│   │   │           │   ├── MyBatisConfig.java           # MyBatis配置
│   │   │           │   ├── MyMetaObjectHandler.java     # 自动填充处理器
│   │   │           │   └── SecurityConfig.java          # 安全配置
│   │   │           ├── controller/                      # 控制器层
│   │   │           │   ├── AuthController.java          # 认证控制器
│   │   │           │   ├── UserController.java          # 用户控制器
│   │   │           │   └── HealthController.java        # 健康检查
│   │   │           ├── dto/                             # 数据传输对象
│   │   │           │   └── auth/                        
│   │   │           │       ├── RegisterRequestDTO.java  # 注册请求
│   │   │           │       ├── LoginRequestDTO.java     # 登录请求
│   │   │           │       ├── LoginResponseDTO.java    # 登录响应
│   │   │           │       ├── UserInfoDTO.java         # 用户信息
│   │   │           │       ├── UpdateUserRequestDTO.java# 更新用户请求
│   │   │           │       └── RefreshTokenRequestDTO.java # 刷新Token
│   │   │           ├── entity/                          # 实体类
│   │   │           │   └── User.java                    # 用户实体
│   │   │           ├── exception/                       # 异常处理
│   │   │           │   ├── BusinessException.java       # 业务异常
│   │   │           │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   │   │           ├── mapper/                          # 数据访问层
│   │   │           │   └── UserMapper.java             # 用户Mapper
│   │   │           ├── security/                        # 安全组件
│   │   │           │   └── JwtAuthenticationFilter.java # JWT过滤器
│   │   │           ├── service/                         # 服务层
│   │   │           │   ├── UserService.java             # 用户服务接口
│   │   │           │   └── impl/                        
│   │   │           │       └── UserServiceImpl.java     # 用户服务实现
│   │   │           └── util/                            # 工具类
│   │   │               ├── JwtUtil.java                 # JWT工具
│   │   │               └── SecurityUtil.java           # 安全工具
│   │   └── resources/
│   │       ├── application.yml                          # 主配置文件
│   │       ├── application-dev.yml                      # 开发环境配置
│   │       └── mapper/                                  # MyBatis映射文件
│   │           └── UserMapper.xml                       # 用户Mapper映射
│   └── test/                                           # 测试代码
│       └── java/
│           └── com/
│               └── codivio/
│                   ├── CodivioApplicationTests.java     # 主测试类
│                   └── auth/                            # 认证模块测试
│                       ├── AuthControllerTest.java     # 认证控制器测试
│                       └── UserServiceTest.java        # 用户服务测试
├── pom.xml                                             # Maven配置文件
├── logs/                                               # 日志目录
└── README.md                                          # 项目说明

## 🔧 功能模块说明

### 认证流程
1. **用户注册**: 
   - 验证用户名和邮箱唯一性
   - 密码加盐哈希存储
   - 返回用户基本信息

2. **用户登录**:
   - 验证用户名密码
   - 生成JWT访问令牌和刷新令牌
   - 返回令牌和用户信息

3. **JWT认证**:
   - 请求头携带Bearer Token
   - JWT过滤器验证令牌有效性
   - 设置安全上下文

4. **用户管理**:
   - 获取当前用户信息
   - 更新用户资料
   - 用户状态管理

### 安全特性
- ✅ 密码加盐哈希存储
- ✅ JWT令牌认证
- ✅ CORS跨域配置
- ✅ 全局异常处理
- ✅ 输入参数验证
- ✅ SQL注入防护（MyBatis Plus）

### API接口
```
POST   /api/v1/auth/register        # 用户注册
POST   /api/v1/auth/login           # 用户登录  
POST   /api/v1/auth/refresh         # 刷新令牌
GET    /api/v1/auth/check-username  # 检查用户名
GET    /api/v1/auth/check-email     # 检查邮箱
GET    /api/v1/users/me             # 获取当前用户
PUT    /api/v1/users/me             # 更新当前用户
GET    /api/v1/users/{id}           # 获取用户信息
GET    /api/v1/health               # 健康检查
GET    /api/v1/health/auth-test     # 认证测试
```