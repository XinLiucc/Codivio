# Codivio 开发环境搭建指南

## 环境要求

- Docker >= 24.0
- Docker Compose >= 2.0
- （本地开发）Java 17、Maven 3.8+、Node.js 18+

## Docker 一键启动

```bash
# 构建镜像并启动所有服务（首次或代码有变更时）
docker compose up -d --build

# 仅启动（镜像已存在）
docker compose up -d
```

访问 http://localhost 即可使用。

## 验证服务状态

```bash
docker compose ps
```

所有服务均应显示 `(healthy)` 状态。启动顺序：
1. MySQL / Redis / RabbitMQ（基础设施，约 30s）
2. user-service（约 60s）
3. project-service / file-service / collaboration-service
4. gateway-service
5. frontend

## 本地开发模式

只启动基础设施，后端/前端在本机运行：

```bash
# 启动基础设施
docker compose up -d mysql redis rabbitmq

# 启动某个后端服务（默认 profile 连接 localhost）
cd backend/user-service
mvn spring-boot:run

# 启动前端（Vite dev server，含代理配置）
cd frontend
npm install
npm run dev
# 访问 http://localhost:3000
```

## 数据库

| 参数 | 值 |
|------|----|
| Host | localhost:3306 |
| 用户名 | codivio |
| 密码 | codivio123 |
| 数据库 | codivio_user / codivio_project / codivio_file |

初始化 SQL 位于 `docker/mysql/init/`，容器首次启动时自动执行。

重置数据库：
```bash
docker compose down -v
docker compose up -d --build
```

## Redis

```bash
# 连接测试
redis-cli -h localhost -p 6379 ping
```

## RabbitMQ

- 管理界面：http://localhost:15672
- 账号：codivio / codivio123

## 常见问题

**某个服务一直 `health: starting`**

查看该服务日志：
```bash
docker compose logs -f <service-name>
```

**端口冲突**

检查本机是否占用了 80 / 3306 / 6379 / 5672 端口：
```bash
ss -tlnp | grep -E '80|3306|6379|5672'
```

**重新构建单个服务**

```bash
docker compose build <service-name>
docker compose up -d <service-name>
```
