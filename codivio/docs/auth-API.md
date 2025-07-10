# 🚀 Codivio Auth模块-API接口文档

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

## 📋 接口概览

**基础信息**
- **Base URL**: `http://localhost:8080/api/v1`
- **认证方式**: JWT Bearer Token
- **请求格式**: JSON
- **响应格式**: JSON
- **字符编码**: UTF-8

## 🔐 认证相关API

### 1. 用户注册

**接口描述**: 用户账号注册

- **URL**: `POST /auth/register`
- **认证**: 无需认证
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "username": "string",     // 用户名，3-50个字符，必填
  "email": "string",        // 邮箱地址，必填
  "password": "string",     // 密码，6-50个字符，必填
  "displayName": "string",  // 显示名称，可选
  "inviteCode": "string"    // 邀请码，可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 10,
    "username": "testuser",
    "email": "test@codivio.dev",
    "displayName": "测试用户",
    "avatarUrl": null,
    "bio": null,
    "emailVerified": false,
    "createdAt": "2025-07-09T17:20:49"
  },
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@codivio.dev",
    "password": "password123",
    "displayName": "新用户"
  }'
```

---

### 2. 用户登录

**接口描述**: 用户账号登录，获取JWT访问令牌

- **URL**: `POST /auth/login`
- **认证**: 无需认证
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "username": "string",  // 用户名，必填
  "password": "string"   // 密码，必填
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "userId": 10,
      "username": "testuser",
      "email": "test@codivio.dev",
      "displayName": "测试用户",
      "avatarUrl": null,
      "bio": null,
      "emailVerified": false,
      "createdAt": "2025-07-09T17:20:49"
    }
  },
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

---

### 3. 刷新Token

**接口描述**: 使用刷新令牌获取新的访问令牌

- **URL**: `POST /auth/refresh`
- **认证**: 无需认证
- **Content-Type**: `application/json`

**请求参数**:
```json
{
  "refreshToken": "string"  // 刷新令牌，必填
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "userId": 10,
      "username": "testuser",
      "email": "test@codivio.dev",
      "displayName": "测试用户"
    }
  },
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

---

### 4. 检查用户名可用性

**接口描述**: 检查用户名是否已被注册

- **URL**: `GET /auth/check-username`
- **认证**: 无需认证

**请求参数**:
| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| username | string | query | 是 | 要检查的用户名 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true,  // true: 可用, false: 已被占用
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl "http://localhost:8080/api/v1/auth/check-username?username=newuser"
```

---

### 5. 检查邮箱可用性

**接口描述**: 检查邮箱是否已被注册

- **URL**: `GET /auth/check-email`
- **认证**: 无需认证

**请求参数**:
| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| email | string | query | 是 | 要检查的邮箱地址 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true,  // true: 可用, false: 已被占用
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl "http://localhost:8080/api/v1/auth/check-email?email=test@codivio.dev"
```

---

## 👤 用户管理API

### 6. 获取当前用户信息

**接口描述**: 获取当前登录用户的详细信息

- **URL**: `GET /users/me`
- **认证**: 需要JWT Token
- **Headers**: `Authorization: Bearer {access_token}`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 10,
    "username": "testuser",
    "email": "test@codivio.dev",
    "displayName": "测试用户",
    "avatarUrl": "https://avatar.example.com/user.png",
    "bio": "这是我的个人简介",
    "emailVerified": false,
    "createdAt": "2025-07-09T17:20:49"
  },
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 7. 更新当前用户信息

**接口描述**: 更新当前登录用户的资料信息

- **URL**: `PUT /users/me`
- **认证**: 需要JWT Token
- **Content-Type**: `application/json`
- **Headers**: `Authorization: Bearer {access_token}`

**请求参数**:
```json
{
  "displayName": "string",  // 显示名称，可选，最长100字符
  "bio": "string",          // 个人简介，可选，最长500字符
  "avatarUrl": "string"     // 头像链接，可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "userId": 10,
    "username": "testuser",
    "email": "test@codivio.dev",
    "displayName": "更新后的显示名",
    "avatarUrl": "https://avatar.example.com/new.png",
    "bio": "更新后的个人简介",
    "emailVerified": false,
    "createdAt": "2025-07-09T17:20:49"
  },
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl -X PUT "http://localhost:8080/api/v1/users/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "displayName": "新的显示名",
    "bio": "这是更新后的个人简介"
  }'
```

---

### 8. 获取指定用户信息

**接口描述**: 获取指定用户的公开信息（不包含邮箱等隐私信息）

- **URL**: `GET /users/{userId}`
- **认证**: 需要JWT Token
- **Headers**: `Authorization: Bearer {access_token}`

**请求参数**:
| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| userId | long | path | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 5,
    "username": "anotheruser",
    "email": null,  // 隐私信息不返回
    "displayName": "另一个用户",
    "avatarUrl": "https://avatar.example.com/user5.png",
    "bio": "这是另一个用户的简介",
    "emailVerified": null,
    "createdAt": "2025-07-08T15:30:20"
  },
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl -X GET "http://localhost:8080/api/v1/users/5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 🏥 健康检查API

### 9. 系统健康检查

**接口描述**: 检查系统运行状态和基本信息

- **URL**: `GET /health`
- **认证**: 无需认证

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "timestamp": "2025-07-09T17:20:49.123456789",
    "service": "codivio-backend",
    "version": "0.1.0-SNAPSHOT"
  },
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl "http://localhost:8080/api/v1/health"
```

---

### 10. 认证测试接口

**接口描述**: 测试JWT认证是否正常工作

- **URL**: `GET /health/auth-test`
- **认证**: 需要JWT Token
- **Headers**: `Authorization: Bearer {access_token}`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "认证测试成功，您已成功通过JWT认证",
  "timestamp": 1752052849077
}
```

**cURL示例**:
```bash
curl -X GET "http://localhost:8080/api/v1/health/auth-test" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 11. Spring Boot Actuator健康检查

**接口描述**: Spring Boot内置的健康检查端点

- **URL**: `GET /actuator/health`
- **认证**: 无需认证

**请求参数**: 无

**响应示例**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.12"
      }
    }
  }
}
```

**cURL示例**:
```bash
curl "http://localhost:8080/api/v1/actuator/health"
```

---

## 🛠️ 调试API（开发环境）

### 12. 数据库连接测试

**接口描述**: 测试数据库连接状态

- **URL**: `GET /debug/test-db`
- **认证**: 无需认证

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "connection": "✅ 数据库连接成功",
    "url": "jdbc:mysql://localhost:3306/codivio_user_dev?...",
    "username": "codivio_dev@172.19.0.1"
  },
  "timestamp": 1752052849077
}
```

---

### 13. MyBatis配置测试

**接口描述**: 测试MyBatis配置和映射

- **URL**: `GET /debug/test-mybatis`
- **认证**: 无需认证

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userMapper": "✅ UserMapper注入成功",
    "userCount": 5,
    "query": "✅ 查询执行成功"
  },
  "timestamp": 1752052849077
}
```

---

### 14. 密码验证测试

**接口描述**: 测试密码加密和验证机制

- **URL**: `GET /debug/test-password`
- **认证**: 无需认证

**请求参数**:
| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| username | string | query | 是 | 用户名 |
| password | string | query | 是 | 密码 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "step1": "✅ 用户存在",
    "step2": "✅ 密码+盐值生成",
    "step3": "✅ 密码验证成功",
    "passwordMatch": true,
    "userId": 10,
    "username": "testuser"
  },
  "timestamp": 1752052849077
}
```

---

## 📊 统一响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success" | "具体成功消息",
  "data": {}, // 具体数据
  "timestamp": 1752052849077
}
```

### 错误响应
```json
{
  "code": 400,  // 错误状态码
  "message": "具体错误信息",
  "data": null,
  "timestamp": 1752052849077
}
```

## 🚨 错误码说明

| 错误码 | 说明 | 常见场景 |
|--------|------|----------|
| 200 | 成功 | 请求正常处理 |
| 400 | 参数错误 | 请求参数缺失或格式错误 |
| 401 | 认证失败 | Token无效、过期或缺失 |
| 403 | 权限不足 | 没有访问权限 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 409 | 资源冲突 | 用户名或邮箱已存在 |
| 429 | 请求过于频繁 | 超出API调用频率限制 |
| 500 | 服务器错误 | 系统内部错误 |

## 🔐 JWT认证说明

### Token格式
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token内容
```json
{
  "sub": "10",              // 用户ID
  "username": "testuser",   // 用户名
  "type": "access",         // Token类型
  "iat": 1752052849,        // 签发时间
  "exp": 1752139249         // 过期时间
}
```

### Token有效期
- **Access Token**: 24小时
- **Refresh Token**: 7天

## 📝 使用示例

### 完整的用户注册登录流程

```bash
#!/bin/bash
BASE_URL="http://localhost:8080/api/v1"

# 1. 检查用户名是否可用
curl "$BASE_URL/auth/check-username?username=newuser"

# 2. 注册新用户
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@codivio.dev",
    "password": "password123",
    "displayName": "新用户"
  }')

# 3. 用户登录
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123"
  }')

# 4. 提取access token
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')

# 5. 获取当前用户信息
curl "$BASE_URL/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# 6. 更新用户信息
curl -X PUT "$BASE_URL/users/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "displayName": "更新后的名称",
    "bio": "这是我的新简介"
  }'
```

## 🧪 前端集成示例

### JavaScript/TypeScript示例

```typescript
// API客户端类
class CodivioAPI {
  private baseURL = 'http://localhost:8080/api/v1';
  private accessToken: string | null = null;

  // 设置认证token
  setToken(token: string) {
    this.accessToken = token;
  }

  // 通用请求方法
  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    return response.json();
  }

  // 用户注册
  async register(userData: {
    username: string;
    email: string;
    password: string;
    displayName?: string;
  }) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  // 用户登录
  async login(credentials: { username: string; password: string }) {
    const response = await this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });

    if (response.code === 200) {
      this.setToken(response.data.accessToken);
    }

    return response;
  }

  // 获取当前用户信息
  async getCurrentUser() {
    return this.request('/users/me');
  }

  // 更新用户信息
  async updateUser(userData: {
    displayName?: string;
    bio?: string;
    avatarUrl?: string;
  }) {
    return this.request('/users/me', {
      method: 'PUT',
      body: JSON.stringify(userData),
    });
  }
}

// 使用示例
const api = new CodivioAPI();

// 登录
const loginResult = await api.login({
  username: 'testuser',
  password: 'password123'
});

if (loginResult.code === 200) {
  console.log('登录成功', loginResult.data.user);
  
  // 获取用户信息
  const userInfo = await api.getCurrentUser();
  console.log('用户信息', userInfo.data);
}
```

## 🎯 总结

目前Codivio已实现的API接口包括：

### ✅ 已完成功能
1. **用户认证系统** - 注册、登录、JWT认证
2. **用户管理** - 获取和更新用户信息
3. **系统监控** - 健康检查和状态监控
4. **开发调试** - 完整的调试接口集

### 🔧 技术特性
- **RESTful设计** - 符合REST API规范
- **JWT认证** - 无状态认证机制
- **统一响应格式** - 一致的JSON响应结构
- **完善的错误处理** - 详细的错误码和消息
- **参数验证** - 完整的输入验证机制

### 🚀 下一步扩展
- 项目管理API
- 实时编辑API
- 文件管理API
- 版本控制API
- 代码执行API

所有接口都经过完整测试，可以支持前端Vue.js应用的开发需求！