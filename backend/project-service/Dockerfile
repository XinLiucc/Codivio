# 多阶段构建 - 构建阶段
FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制构建产物
COPY --from=builder /app/target/project-service-1.0.0.jar app.jar

# 添加健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# JVM优化参数和Docker兼容性参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Djdk.internal.platform.cgroupv1.useSystemdMemoryController=false"

# 暴露端口
EXPOSE 8082

# 运行应用
CMD java $JAVA_OPTS -jar app.jar