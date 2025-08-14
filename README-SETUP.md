# Codivio 开发环境搭建指南

## 🐳 Docker 环境启动

### 1. 启动基础服务

```bash
# 启动所有基础服务（MySQL、Redis、RabbitMQ）
docker-compose up -d mysql redis rabbitmq

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs mysql
docker-compose logs redis
docker-compose logs rabbitmq
```

### 2. 验证服务

#### MySQL 验证
```bash
# 连接数据库
mysql -h localhost -P 3306 -u codivio -p
# 密码：codivio123

# 检查数据库
SHOW DATABASES;
USE codivio_user;
SHOW TABLES;
```

#### Redis 验证
```bash
# 连接 Redis
redis-cli -h localhost -p 6379
# 测试
ping
```

#### RabbitMQ 验证
- 管理界面：http://localhost:15672
- 用户名：codivio
- 密码：codivio123

### 3. 环境配置

所有配置参数都在 `.env` 文件中定义：

```bash
# 查看配置
cat .env
```

## 📊 数据库结构

### 用户服务数据库 (codivio_user)
- `users` - 用户基础信息表
- `user_preferences` - 用户偏好设置表

### 项目服务数据库 (codivio_project)  
- `projects` - 项目基础信息表
- `project_members` - 项目成员表
- `project_files` - 项目文件表

## 🔧 开发工具

### 数据库管理
- **DBeaver** - 可视化数据库管理工具
- **连接参数**：
  - Host: localhost
  - Port: 3306
  - Username: codivio
  - Password: codivio123

### Redis 管理
- **RedisInsight** - Redis 可视化管理工具
- **连接参数**：
  - Host: localhost
  - Port: 6379

## 🚀 下一步

基础环境已经搭建完成，接下来可以：

1. 开发用户服务 (Spring Boot)
2. 开发网关服务 (Spring Cloud Gateway)
3. 开发项目服务 (Spring Boot)
4. 开发协作服务 (WebSocket)
5. 开发前端应用 (Vue 3)

## 📝 注意事项

- 首次启动 MySQL 会自动执行初始化脚本
- 如需重置数据库，删除 Docker volume：`docker-compose down -v`
- 所有服务使用统一网络：`codivio-network`