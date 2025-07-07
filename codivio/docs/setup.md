# 环境搭建指南

## 🎯 系统要求

### 开发环境要求
- **JDK**: Java 17+ (推荐使用OpenJDK 17)
- **Node.js**: 18.0+ (推荐使用18.17.0 LTS)
- **Maven**: 3.8+ (或使用项目内置mvnw)
- **Docker**: 20.10+ (用于代码执行环境)
- **Git**: 2.30+

### 数据库要求
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **MongoDB**: 6.0+ (可选，用于日志存储)

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/XinLiucc/Codivio.git
cd Codivio
```

### 2. 后端环境搭建

#### 安装Java 17
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# macOS (使用Homebrew)
brew install openjdk@17

# Windows (使用Chocolatey)
choco install openjdk17
```

#### 配置数据库
```sql
-- 创建数据库
CREATE DATABASE codivio CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'codivio'@'localhost' IDENTIFIED BY 'codivio123';
GRANT ALL PRIVILEGES ON codivio.* TO 'codivio'@'localhost';
FLUSH PRIVILEGES;
```

#### 配置application.yml
```yaml
# backend/src/main/resources/application-dev.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/codivio?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
    username: codivio
    password: codivio123
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    
  profiles:
    active: dev

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

#### 启动后端服务
```bash
cd backend
./mvnw spring-boot:run
```

### 3. 前端环境搭建

#### 安装Node.js和pnpm
```bash
# 安装Node.js 18
nvm install 18
nvm use 18

# 安装pnpm
npm install -g pnpm

# 或使用npm
npm install -g npm@latest
```

#### 安装依赖
```bash
cd frontend
pnpm install

# 或使用npm
npm install
```

#### 配置环境变量
```bash
# frontend/.env.development
VITE_APP_BASE_URL=http://localhost:8080
VITE_APP_WS_URL=ws://localhost:8080/ws
VITE_APP_TITLE=Codivio Development
```

#### 启动前端服务
```bash
pnpm dev

# 或使用npm
npm run dev
```

### 4. Docker环境配置

#### 安装Docker
```bash
# Ubuntu
sudo apt install docker.io docker-compose

# macOS
brew install docker

# 启动Docker服务
sudo systemctl start docker
```

#### 使用Docker Compose快速启动
```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: codivio-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: codivio
      MYSQL_USER: codivio
      MYSQL_PASSWORD: codivio123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    container_name: codivio-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

```bash
# 启动基础服务
docker-compose -f docker-compose.dev.yml up -d
```

## 🔧 IDE配置

### IntelliJ IDEA (推荐)
```xml
<!-- .idea/runConfigurations/Codivio_Backend.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Codivio Backend" type="SpringBootApplicationConfigurationType">
    <option name="SPRING_BOOT_MAIN_CLASS" value="com.codivio.CodivioApplication" />
    <option name="ALTERNATIVE_JRE_PATH" value="17" />
    <option name="PROGRAM_PARAMETERS" value="--spring.profiles.active=dev" />
  </configuration>
</component>
```

### VSCode配置
```json
// .vscode/settings.json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/usr/lib/jvm/java-17-openjdk"
    }
  ],
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.java.vmargs": "-XX:TieredStopAtLevel=1 -Xmx1G",
  "typescript.preferences.importModuleSpecifier": "relative"
}
```

## 📋 验证安装

### 后端验证
```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查Spring Boot启动
curl http://localhost:8080/actuator/health
```

### 前端验证
```bash
# 检查Node.js版本
node -v

# 检查pnpm版本
pnpm -v

# 检查前端服务
curl http://localhost:3000
```

### 数据库连接验证
```bash
# MySQL连接测试
mysql -u codivio -p codivio

# Redis连接测试
redis-cli ping
```

## 🐛 常见问题

### Java相关
**问题**: `JAVA_HOME` 环境变量未设置
```bash
# 解决方案
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> ~/.bashrc
```

### Node.js相关
**问题**: npm安装依赖失败
```bash
# 清理缓存
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Docker相关
**问题**: Docker权限问题
```bash
# 添加用户到docker组
sudo usermod -aG docker $USER
# 重新登录或使用newgrp docker
```