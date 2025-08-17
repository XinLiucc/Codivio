# 使用官方的 OpenJDK 17 基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml .

# 复制源代码
COPY src ./src

# 复制 Maven Wrapper（如果有的话）
COPY mvnw .
COPY .mvn ./.mvn

# 安装 Maven（如果镜像中没有）
RUN apt-get update && apt-get install -y maven

# 构建应用
RUN mvn clean package -DskipTests

# 运行应用
CMD ["java", "-jar", "target/collaboration-service-1.0.0.jar"]

# 暴露端口
EXPOSE 8083