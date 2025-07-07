# API接口规范

## 🔗 接口概览

### 基础信息
- **Base URL**: `https://api.codivio.dev/v1`
- **认证方式**: JWT Bearer Token
- **请求格式**: JSON
- **响应格式**: JSON
- **字符编码**: UTF-8

### 通用响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1640995200000,
  "traceId": "trace_12345"
}
```

### 错误码定义
| 错误码 | 说明 | 示例 |
|--------|------|------|
| 200 | 成功 | 操作成功 |
| 400 | 参数错误 | 缺少必要参数 |
| 401 | 认证失败 | Token无效或过期 |
| 403 | 权限不足 | 无访问权限 |
| 404 | 资源不存在 | 项目不存在 |
| 429 | 请求过于频繁 | 超出速率限制 |
| 500 | 服务器内部错误 | 系统异常 |

## 👤 用户管理API

### 用户注册
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "inviteCode": "INVITE123" // 可选
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": "12345",
    "username": "john_doe",
    "email": "john@example.com",
    "avatar": "https://avatar.codivio.dev/default.png",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### 用户登录
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "user": {
      "userId": "12345",
      "username": "john_doe",
      "email": "john@example.com",
      "avatar": "https://avatar.codivio.dev/john.png"
    }
  }
}
```

### 获取用户信息
```http
GET /users/me
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "12345",
    "username": "john_doe",
    "email": "john@example.com",
    "avatar": "https://avatar.codivio.dev/john.png",
    "bio": "Full-stack developer",
    "location": "San Francisco, CA",
    "website": "https://johndoe.dev",
    "githubUsername": "johndoe",
    "createdAt": "2024-01-01T00:00:00Z",
    "lastLoginAt": "2024-01-15T10:30:00Z"
  }
}
```

### 更新用户信息
```http
PUT /users/me
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "bio": "Senior Full-stack Developer",
  "location": "New York, NY",
  "website": "https://johnsmith.dev"
}
```

## 📁 项目管理API

### 创建项目
```http
POST /projects
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "My Awesome Project",
  "description": "A collaborative coding project",
  "language": "javascript",
  "template": "react-typescript", // 可选
  "visibility": "private" // private | public
}
```

**响应**:
```json
{
  "code": 200,
  "message": "项目创建成功",
  "data": {
    "projectId": "proj_abc123",
    "name": "My Awesome Project",
    "description": "A collaborative coding project",
    "language": "javascript",
    "visibility": "private",
    "owner": {
      "userId": "12345",
      "username": "john_doe"
    },
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 获取项目列表
```http
GET /projects?page=1&size=20&language=javascript&visibility=all&sort=updatedAt&order=desc
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "projects": [
      {
        "projectId": "proj_abc123",
        "name": "My Awesome Project",
        "description": "A collaborative coding project",
        "language": "javascript",
        "visibility": "private",
        "owner": {
          "userId": "12345",
          "username": "john_doe"
        },
        "memberCount": 3,
        "lastActivity": "2024-01-15T10:30:00Z",
        "createdAt": "2024-01-10T08:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 5,
      "totalPages": 1
    }
  }
}
```

### 获取项目详情
```http
GET /projects/{projectId}
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "projectId": "proj_abc123",
    "name": "My Awesome Project",
    "description": "A collaborative coding project",
    "language": "javascript",
    "visibility": "private",
    "owner": {
      "userId": "12345",
      "username": "john_doe",
      "avatar": "https://avatar.codivio.dev/john.png"
    },
    "members": [
      {
        "userId": "12345",
        "username": "john_doe",
        "role": "owner",
        "joinedAt": "2024-01-10T08:00:00Z"
      },
      {
        "userId": "67890",
        "username": "jane_smith",
        "role": "editor",
        "joinedAt": "2024-01-12T14:20:00Z"
      }
    ],
    "settings": {
      "autoSave": true,
      "codeExecution": true,
      "publicAccess": false
    },
    "stats": {
      "fileCount": 15,
      "commitCount": 23,
      "collaboratorCount": 2,
      "lastActivity": "2024-01-15T10:30:00Z"
    },
    "createdAt": "2024-01-10T08:00:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 文件系统API

#### 获取文件树
```http
GET /projects/{projectId}/files
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "root": {
      "name": "/",
      "type": "directory",
      "children": [
        {
          "name": "src",
          "type": "directory",
          "children": [
            {
              "name": "App.js",
              "type": "file",
              "size": 1024,
              "lastModified": "2024-01-15T10:30:00Z"
            },
            {
              "name": "index.js",
              "type": "file",
              "size": 512,
              "lastModified": "2024-01-14T16:20:00Z"
            }
          ]
        },
        {
          "name": "package.json",
          "type": "file",
          "size": 256,
          "lastModified": "2024-01-10T08:00:00Z"
        }
      ]
    }
  }
}
```

#### 获取文件内容
```http
GET /projects/{projectId}/files/content?path=/src/App.js
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "path": "/src/App.js",
    "content": "import React from 'react';\n\nfunction App() {\n  return (\n    <div className=\"App\">\n      <h1>Hello, Codivio!</h1>\n    </div>\n  );\n}\n\nexport default App;",
    "encoding": "utf-8",
    "size": 1024,
    "lastModified": "2024-01-15T10:30:00Z"
  }
}
```

#### 保存文件
```http
PUT /projects/{projectId}/files/content
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "path": "/src/App.js",
  "content": "import React from 'react';\n\nfunction App() {\n  return (\n    <div className=\"App\">\n      <h1>Hello, Codivio World!</h1>\n    </div>\n  );\n}\n\nexport default App;",
  "encoding": "utf-8"
}
```

## 🔄 实时协作API

### WebSocket连接
```javascript
// WebSocket连接地址
const wsUrl = 'wss://api.codivio.dev/ws/editor';
const socket = new WebSocket(wsUrl, ['authorization', accessToken]);

// 连接建立
socket.onopen = function(event) {
  console.log('WebSocket连接已建立');
  
  // 加入文档房间
  socket.send(JSON.stringify({
    type: 'join-document',
    data: {
      documentId: 'doc_123',
      userId: '12345',
      userName: 'john_doe'
    }
  }));
};
```

### 消息格式规范

#### 加入文档
```json
{
  "type": "join-document",
  "data": {
    "documentId": "doc_123",
    "userId": "12345",
    "userName": "john_doe"
  }
}
```

#### 编辑操作
```json
{
  "type": "operation",
  "data": {
    "documentId": "doc_123",
    "operation": {
      "type": "insert",
      "position": 100,
      "text": "Hello, World!",
      "clientId": "client_456",
      "timestamp": 1640995200000
    }
  }
}
```

#### 光标位置更新
```json
{
  "type": "cursor-position",
  "data": {
    "documentId": "doc_123",
    "userId": "12345",
    "position": 150,
    "selection": {
      "start": 150,
      "end": 165
    }
  }
}
```

#### 服务器响应消息

##### 文档状态
```json
{
  "type": "document-state",
  "data": {
    "documentId": "doc_123",
    "content": "console.log('Hello, Codivio!');",
    "version": 42,
    "activeUsers": [
      {
        "userId": "12345",
        "userName": "john_doe",
        "cursorColor": "#ff6b6b"
      }
    ]
  }
}
```

##### 操作广播
```json
{
  "type": "operation",
  "data": {
    "operation": {
      "type": "insert",
      "position": 105,
      "text": " Amazing",
      "clientId": "client_789",
      "timestamp": 1640995201000
    },
    "fromUser": "67890"
  }
}
```

## ⚡ 代码执行API

### 执行代码
```http
POST /execution/run
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "language": "javascript",
  "code": "console.log('Hello, Codivio!');\nconsole.log(2 + 3);",
  "input": "", // 标准输入（可选）
  "timeout": 30, // 超时时间（秒）
  "memoryLimit": 128 // 内存限制（MB）
}
```

**响应**:
```json
{
  "code": 200,
  "message": "执行完成",
  "data": {
    "success": true,
    "output": "Hello, Codivio!\n5\n",
    "error": "",
    "exitCode": 0,
    "executionTime": 145, // 毫秒
    "memoryUsage": 12, // MB
    "executionId": "exec_1640995200_abc123"
  }
}
```

### 获取支持的语言
```http
GET /execution/languages
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "languages": [
      {
        "id": "javascript",
        "name": "JavaScript",
        "version": "Node.js 18.17.0",
        "fileExtension": ".js",
        "defaultCode": "console.log('Hello, World!');"
      },
      {
        "id": "python",
        "name": "Python",
        "version": "3.11.0",
        "fileExtension": ".py",
        "defaultCode": "print('Hello, World!')"
      },
      {
        "id": "java",
        "name": "Java",
        "version": "OpenJDK 17",
        "fileExtension": ".java",
        "defaultCode": "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}"
      }
    ]
  }
}
```

## 📊 版本控制API

### 提交代码
```http
POST /projects/{projectId}/git/commit
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "message": "Add new feature implementation",
  "files": [
    {
      "path": "/src/App.js",
      "content": "// Updated content...",
      "action": "modified"
    },
    {
      "path": "/src/NewFeature.js",
      "content": "// New file content...",
      "action": "added"
    }
  ],
  "author": {
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "commitHash": "a1b2c3d4e5f6789012345678901234567890abcd",
    "message": "Add new feature implementation",
    "author": {
      "name": "John Doe",
      "email": "john@example.com"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "changedFiles": ["/src/App.js", "/src/NewFeature.js"],
    "stats": {
      "additions": 45,
      "deletions": 12,
      "filesChanged": 2
    }
  }
}
```

### 获取提交历史
```http
GET /projects/{projectId}/git/commits?branch=main&page=1&size=20
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "commits": [
      {
        "hash": "a1b2c3d4e5f6789012345678901234567890abcd",
        "shortHash": "a1b2c3d",
        "message": "Add new feature implementation",
        "author": {
          "name": "John Doe",
          "email": "john@example.com"
        },
        "timestamp": "2024-01-15T10:30:00Z",
        "stats": {
          "additions": 45,
          "deletions": 12,
          "filesChanged": 2
        }
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 23,
      "totalPages": 2
    }
  }
}
```

### 获取分支列表
```http
GET /projects/{projectId}/git/branches
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "branches": [
      {
        "name": "main",
        "type": "local",
        "isDefault": true,
        "lastCommit": {
          "hash": "a1b2c3d4e5f6789012345678901234567890abcd",
          "message": "Add new feature implementation",
          "timestamp": "2024-01-15T10:30:00Z"
        }
      },
      {
        "name": "feature/new-ui",
        "type": "local", 
        "isDefault": false,
        "lastCommit": {
          "hash": "b2c3d4e5f6789012345678901234567890abcde1",
          "message": "Update UI components",
          "timestamp": "2024-01-14T16:20:00Z"
        }
      }
    ]
  }
}
```

## 🔍 搜索和统计API

### 全局搜索
```http
GET /search?q=React&type=all&page=1&size=20
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "results": {
      "projects": [
        {
          "projectId": "proj_abc123",
          "name": "React Todo App",
          "description": "A simple todo app built with React",
          "language": "javascript",
          "owner": "john_doe",
          "highlightedFields": ["name", "description"]
        }
      ],
      "files": [
        {
          "projectId": "proj_abc123",
          "path": "/src/App.jsx",
          "content": "import React from 'react';...",
          "highlightedLines": [1, 5, 12]
        }
      ],
      "users": [
        {
          "userId": "12345",
          "username": "react_developer",
          "bio": "React specialist",
          "highlightedFields": ["username", "bio"]
        }
      ]
    },
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 15,
      "totalPages": 1
    }
  }
}
```

### 获取统计信息
```http
GET /stats/dashboard
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "overview": {
      "totalProjects": 25,
      "totalCollaborations": 147,
      "totalCodeExecutions": 892,
      "totalCommits": 456
    },
    "recent": {
      "recentProjects": [
        {
          "projectId": "proj_abc123",
          "name": "My Awesome Project",
          "lastActivity": "2024-01-15T10:30:00Z"
        }
      ],
      "recentCollaborations": [
        {
          "projectId": "proj_abc123",
          "collaborator": "jane_smith",
          "activity": "编辑了 App.js",
          "timestamp": "2024-01-15T10:30:00Z"
        }
      ]
    },
    "charts": {
      "dailyActivity": [
        {"date": "2024-01-10", "projects": 3, "collaborations": 12},
        {"date": "2024-01-11", "projects": 2, "collaborations": 8}
      ],
      "languageDistribution": [
        {"language": "javascript", "count": 15, "percentage": 60},
        {"language": "python", "count": 6, "percentage": 24},
        {"language": "java", "count": 4, "percentage": 16}
      ]
    }
  }
}
```

## 🔧 Rate Limiting

### 限流规则
| 端点类型 | 限制 | 时间窗口 |
|----------|------|----------|
| 认证 | 5次/分钟 | 每IP |
| 一般API | 100次/分钟 | 每用户 |
| 代码执行 | 10次/分钟 | 每用户 |
| WebSocket | 1000条/分钟 | 每连接 |

### 限流响应头
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
```

### 限流错误响应
```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": {
    "retryAfter": 60,
    "limit": 100,
    "remaining": 0,
    "resetTime": "2024-01-15T10:31:00Z"
  }
}
```