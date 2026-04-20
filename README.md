# Codivio - 分布式实时协作开发平台

基于微服务架构的在线代码协作平台，支持多人实时编辑、项目管理、文件管理等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Monaco Editor + Yjs |
| 网关 | Spring Cloud Gateway + JWT 认证 |
| 后端 | Spring Boot 3.x + Java 17 |
| 数据库 | MySQL 8.0 + Redis 7 |
| 消息队列 | RabbitMQ 3 |
| 部署 | Docker + Docker Compose |

## 快速启动

**前提**：已安装 Docker 和 Docker Compose

```bash
# 一键构建并启动所有服务
docker compose up -d --build

# 代码无变更时，直接启动
docker compose up -d
```

启动完成后访问 **http://localhost**

> 首次启动需要构建所有镜像，耗时约 5~10 分钟（取决于网络速度）。

## 服务说明

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost | Vue 3 前端应用 |
| 网关 | http://localhost:8080 | Spring Cloud Gateway |
| 用户服务 | http://localhost:8081 | 注册、登录、用户信息 |
| 项目服务 | http://localhost:8082 | 项目 CRUD、成员管理 |
| 协作服务 | http://localhost:8083 | WebSocket 实时协作 |
| 文件服务 | http://localhost:8084 | 文件树、文件内容管理 |
| RabbitMQ 管理 | http://localhost:15672 | 账号 codivio / codivio123 |

## 常用命令

```bash
# 查看所有容器状态
docker compose ps

# 查看某个服务日志
docker compose logs -f user-service

# 停止所有服务
docker compose down

# 停止并删除数据卷（重置数据库）
docker compose down -v

# 单独重建某个服务
docker compose build user-service
docker compose up -d user-service
```

## 项目结构

```
Codivio/
├── backend/
│   ├── gateway-service/       # API 网关（端口 8080）
│   ├── user-service/          # 用户服务（端口 8081）
│   ├── project-service/       # 项目服务（端口 8082）
│   ├── collaboration-service/ # 协作服务（端口 8083）
│   └── file-service/          # 文件服务（端口 8084）
├── frontend/                  # Vue 3 前端
├── docker/
│   └── mysql/init/            # MySQL 初始化 SQL
├── data/files/                # 文件存储目录
└── docker-compose.yml
```

## 数据库

MySQL 初始化脚本在 `docker/mysql/init/` 目录下，首次启动自动执行，创建以下数据库：

- `codivio_user` — 用户信息
- `codivio_project` — 项目与文件元数据
- `codivio_file` — 文件内容

## 本地开发

如需单独开发某个服务，只需启动基础设施：

```bash
docker compose up -d mysql redis rabbitmq
```

然后在 IDE 中以默认 profile 启动对应的 Spring Boot 服务，前端运行：

```bash
cd frontend
npm install
npm run dev
```

---

**联系方式**: lxin233@163.com
