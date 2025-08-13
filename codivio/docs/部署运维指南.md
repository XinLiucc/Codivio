# 部署指南

## 🎯 部署架构概览

### 部署环境分层
```
Production Environment (生产环境)
├── Load Balancer (负载均衡器)
│   ├── Nginx/HAProxy
│   └── SSL终止和缓存
├── Kubernetes Cluster (K8s集群)
│   ├── Frontend Pods (前端Pod)
│   ├── Backend Pods (后端Pod)
│   ├── Gateway Pod (网关Pod)
│   └── Worker Pods (工作节点Pod)
├── Database Layer (数据库层)
│   ├── MySQL Cluster (主从复制)
│   ├── Redis Cluster (集群模式)
│   └── MongoDB ReplicaSet (副本集)
└── Infrastructure (基础设施)
    ├── Monitoring (监控系统)
    ├── Logging (日志系统)
    └── Backup (备份系统)
```

## 🐳 Docker容器化

### Dockerfile配置

#### 后端Dockerfile
```dockerfile
# backend/Dockerfile
FROM openjdk:17-jdk-alpine AS builder

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jre-alpine

# 添加应用用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 安装必要工具
RUN apk add --no-cache curl

# 创建应用目录
WORKDIR /app
RUN chown -R appuser:appgroup /app

# 复制应用文件
COPY --from=builder /app/target/*.jar app.jar
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 切换到非root用户
USER appuser

EXPOSE 8080

ENTRYPOINT ["./docker-entrypoint.sh"]
```

#### 前端Dockerfile
```dockerfile
# frontend/Dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制依赖文件
COPY package*.json ./
COPY pnpm-lock.yaml ./

# 安装pnpm
RUN npm install -g pnpm

# 安装依赖
RUN pnpm install --frozen-lockfile

# 复制源代码
COPY . .

# 构建应用
RUN pnpm build

# 生产阶段
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 添加健康检查
RUN apk add --no-cache curl
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:80 || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

#### Docker Compose开发环境
```yaml
# docker-compose.yml
version: '3.8'

services:
  # 后端服务
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
      - MONGODB_HOST=mongodb
    depends_on:
      - mysql
      - redis
      - mongodb
    volumes:
      - ./logs:/app/logs
    networks:
      - codivio-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 前端服务
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - codivio-network

  # MySQL数据库
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: codivio
      MYSQL_USER: codivio
      MYSQL_PASSWORD: codivio123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/mysql/init:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password
    networks:
      - codivio-network

  # Redis缓存
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes --requirepass redis123
    networks:
      - codivio-network

  # MongoDB文档数据库
  mongodb:
    image: mongo:6.0
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: admin123
      MONGO_INITDB_DATABASE: codivio_logs
    volumes:
      - mongodb_data:/data/db
    networks:
      - codivio-network

  # Nginx负载均衡
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
    depends_on:
      - frontend
      - backend
    networks:
      - codivio-network

volumes:
  mysql_data:
  redis_data:
  mongodb_data:

networks:
  codivio-network:
    driver: bridge
```

## ☸️ Kubernetes部署

### Namespace和ConfigMap
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: codivio
  labels:
    name: codivio
    environment: production

---
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: codivio-config
  namespace: codivio
data:
  # 应用配置
  spring.profiles.active: "kubernetes"
  server.port: "8080"
  
  # 数据库配置
  mysql.host: "mysql-service"
  mysql.port: "3306"
  mysql.database: "codivio"
  
  # Redis配置
  redis.host: "redis-service"
  redis.port: "6379"
  
  # MongoDB配置
  mongodb.host: "mongodb-service"
  mongodb.port: "27017"
  mongodb.database: "codivio_logs"
  
  # 日志配置
  logging.level.com.codivio: "INFO"
  logging.file.name: "/app/logs/application.log"
```

### Secret管理
```yaml
# k8s/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: codivio-secrets
  namespace: codivio
type: Opaque
data:
  # Base64编码的密码
  mysql-username: Y29kaXZpbw==     # codivio
  mysql-password: Y29kaXZpbzEyMw== # codivio123
  redis-password: cmVkaXMxMjM=     # redis123
  mongodb-username: YWRtaW4=       # admin
  mongodb-password: YWRtaW4xMjM=   # admin123
  jwt-secret: bXlfc3VwZXJfc2VjcmV0X2tleV9mb3JfY29kaXZpbw== # jwt secret
```

### 后端服务部署
```yaml
# k8s/backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: codivio-backend
  namespace: codivio
  labels:
    app: codivio-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: codivio-backend
  template:
    metadata:
      labels:
        app: codivio-backend
    spec:
      containers:
      - name: backend
        image: codivio/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: codivio-config
              key: spring.profiles.active
        - name: MYSQL_HOST
          valueFrom:
            configMapKeyRef:
              name: codivio-config
              key: mysql.host
        - name: MYSQL_USERNAME
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: mysql-username
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: mysql-password
        - name: REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: codivio-config
              key: redis.host
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: redis-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        volumeMounts:
        - name: logs
          mountPath: /app/logs
      volumes:
      - name: logs
        emptyDir: {}

---
# k8s/backend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: codivio-backend-service
  namespace: codivio
spec:
  selector:
    app: codivio-backend
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

### 前端服务部署
```yaml
# k8s/frontend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: codivio-frontend
  namespace: codivio
  labels:
    app: codivio-frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: codivio-frontend
  template:
    metadata:
      labels:
        app: codivio-frontend
    spec:
      containers:
      - name: frontend
        image: codivio/frontend:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "64Mi"
            cpu: "50m"
          limits:
            memory: "128Mi"
            cpu: "100m"
        livenessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 10

---
# k8s/frontend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: codivio-frontend-service
  namespace: codivio
spec:
  selector:
    app: codivio-frontend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 80
  type: ClusterIP
```

### 数据库部署

#### MySQL部署
```yaml
# k8s/mysql-deployment.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: codivio
spec:
  serviceName: mysql-service
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: mysql-password
        - name: MYSQL_DATABASE
          value: "codivio"
        - name: MYSQL_USER
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: mysql-username
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: mysql-password
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
  volumeClaimTemplates:
  - metadata:
      name: mysql-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 20Gi

---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: codivio
spec:
  selector:
    app: mysql
  ports:
  - port: 3306
    targetPort: 3306
  clusterIP: None
```

#### Redis集群部署
```yaml
# k8s/redis-deployment.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis
  namespace: codivio
spec:
  serviceName: redis-service
  replicas: 3
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        command:
        - redis-server
        - /etc/redis/redis.conf
        - --requirepass
        - $(REDIS_PASSWORD)
        env:
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: codivio-secrets
              key: redis-password
        ports:
        - containerPort: 6379
        volumeMounts:
        - name: redis-storage
          mountPath: /data
        - name: redis-config
          mountPath: /etc/redis
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "200m"
      volumes:
      - name: redis-config
        configMap:
          name: redis-config
  volumeClaimTemplates:
  - metadata:
      name: redis-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 5Gi

---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: codivio
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
  clusterIP: None
```

### Ingress配置
```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: codivio-ingress
  namespace: codivio
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
    nginx.ingress.kubernetes.io/proxy-connect-timeout: "600"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "600"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "600"
    nginx.ingress.kubernetes.io/websocket-services: "codivio-backend-service"
spec:
  tls:
  - hosts:
    - codivio.dev
    - api.codivio.dev
    secretName: codivio-tls
  rules:
  - host: codivio.dev
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: codivio-frontend-service
            port:
              number: 80
  - host: api.codivio.dev
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: codivio-backend-service
            port:
              number: 8080
```

## 🚀 CI/CD流水线

### GitHub Actions配置
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test123
          MYSQL_DATABASE: codivio_test
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: --health-cmd="redis-cli ping" --health-interval=10s --health-timeout=5s --health-retries=3

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'adopt'

    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json

    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    # 后端测试
    - name: Run backend tests
      run: |
        cd backend
        ./mvnw clean test
        ./mvnw jacoco:report

    # 前端测试
    - name: Install frontend dependencies
      run: |
        cd frontend
        npm ci

    - name: Run frontend tests
      run: |
        cd frontend
        npm run test:ci
        npm run build

    # 代码质量检查
    - name: SonarQube Scan
      uses: sonarqube-quality-gate-action@master
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}

    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v4
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix={{branch}}-

    # 构建并推送后端镜像
    - name: Build and push backend image
      uses: docker/build-push-action@v4
      with:
        context: ./backend
        push: true
        tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/backend:${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}

    # 构建并推送前端镜像
    - name: Build and push frontend image
      uses: docker/build-push-action@v4
      with:
        context: ./frontend
        push: true
        tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/frontend:${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'v1.28.0'

    - name: Configure kubectl
      run: |
        echo "${{ secrets.KUBE_CONFIG }}" | base64 -d > kubeconfig
        export KUBECONFIG=kubeconfig

    - name: Deploy to Kubernetes
      run: |
        kubectl apply -f k8s/
        kubectl rollout restart deployment/codivio-backend -n codivio
        kubectl rollout restart deployment/codivio-frontend -n codivio
        kubectl rollout status deployment/codivio-backend -n codivio
        kubectl rollout status deployment/codivio-frontend -n codivio

    - name: Run health checks
      run: |
        kubectl wait --for=condition=ready pod -l app=codivio-backend -n codivio --timeout=300s
        kubectl wait --for=condition=ready pod -l app=codivio-frontend -n codivio --timeout=300s
```

## 📊 监控和日志

### Prometheus监控配置
```yaml
# monitoring/prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s

    rule_files:
      - "/etc/prometheus/rules/*.yml"

    alerting:
      alertmanagers:
        - static_configs:
            - targets:
              - alertmanager:9093

    scrape_configs:
      - job_name: 'kubernetes-apiservers'
        kubernetes_sd_configs:
        - role: endpoints
        scheme: https
        tls_config:
          ca_file: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
        bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token
        relabel_configs:
        - source_labels: [__meta_kubernetes_namespace, __meta_kubernetes_service_name, __meta_kubernetes_endpoint_port_name]
          action: keep
          regex: default;kubernetes;https

      - job_name: 'codivio-backend'
        kubernetes_sd_configs:
        - role: pod
          namespaces:
            names:
            - codivio
        relabel_configs:
        - source_labels: [__meta_kubernetes_pod_label_app]
          action: keep
          regex: codivio-backend
        - source_labels: [__meta_kubernetes_pod_ip]
          target_label: __address__
          replacement: ${1}:8080
        - source_labels: [__meta_kubernetes_pod_name]
          target_label: instance
        metrics_path: '/actuator/prometheus'

      - job_name: 'mysql-exporter'
        static_configs:
        - targets: ['mysql-exporter:9104']

      - job_name: 'redis-exporter'
        static_configs:
        - targets: ['redis-exporter:9121']

---
# monitoring/prometheus-rules.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-rules
  namespace: monitoring
data:
  codivio.yml: |
    groups:
    - name: codivio.rules
      rules:
      - alert: HighMemoryUsage
        expr: container_memory_usage_bytes{pod=~"codivio-.*"} / container_spec_memory_limit_bytes > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage detected"
          description: "Pod {{ $labels.pod }} memory usage is above 80%"

      - alert: HighCPUUsage
        expr: rate(container_cpu_usage_seconds_total{pod=~"codivio-.*"}[5m]) > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage detected"
          description: "Pod {{ $labels.pod }} CPU usage is above 80%"

      - alert: ServiceDown
        expr: up{job=~"codivio-.*"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service is down"
          description: "Service {{ $labels.job }} has been down for more than 1 minute"

      - alert: DatabaseConnectionFail
        expr: mysql_up == 0
        for: 30s
        labels:
          severity: critical
        annotations:
          summary: "Database connection failed"
          description: "MySQL database is not responding"
```

### Grafana仪表板
```json
{
  "dashboard": {
    "title": "Codivio Monitoring Dashboard",
    "panels": [
      {
        "title": "System Overview",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"codivio-backend\"}",
            "legendFormat": "Backend Status"
          },
          {
            "expr": "up{job=\"mysql\"}",
            "legendFormat": "Database Status"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_sum{job=\"codivio-backend\"}[5m]) / rate(http_server_requests_seconds_count{job=\"codivio-backend\"}[5m])",
            "legendFormat": "Average Response Time"
          }
        ]
      },
      {
        "title": "Active Users",
        "type": "graph",
        "targets": [
          {
            "expr": "codivio_active_users_total",
            "legendFormat": "Active Users"
          }
        ]
      },
      {
        "title": "Collaboration Operations",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(codivio_collaboration_operations_total[5m])",
            "legendFormat": "Operations per second"
          }
        ]
      }
    ]
  }
}
```

### ELK日志配置
```yaml
# logging/filebeat-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
  namespace: logging
data:
  filebeat.yml: |
    filebeat.inputs:
    - type: container
      paths:
        - '/var/lib/docker/containers/*/*.log'
      processors:
        - add_kubernetes_metadata:
            host: ${NODE_NAME}
            matchers:
            - logs_path:
                logs_path: "/var/lib/docker/containers/"

    output.elasticsearch:
      hosts: ["elasticsearch:9200"]
      index: "codivio-logs-%{+yyyy.MM.dd}"

    setup.template.name: "codivio"
    setup.template.pattern: "codivio-*"
```

## 🔒 安全配置

### 网络安全策略
```yaml
# security/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: codivio-network-policy
  namespace: codivio
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    - podSelector:
        matchLabels:
          app: codivio-frontend
    - podSelector:
        matchLabels:
          app: codivio-backend
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: mysql
  - to:
    - podSelector:
        matchLabels:
          app: redis
  - to: []
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

### Pod安全策略
```yaml
# security/pod-security-policy.yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: codivio-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
```

## 📈 性能优化

### 应用性能优化
```yaml
# 后端JVM优化参数
JAVA_OPTS: >
  -server
  -Xms1g -Xmx2g
  -XX:+UseG1GC
  -XX:G1HeapRegionSize=16m
  -XX:+UseStringDeduplication
  -XX:+OptimizeStringConcat
  -XX:+UseCompressedOops
  -Djava.awt.headless=true
  -Dfile.encoding=UTF-8
  -Dspring.backgroundpreinitializer.ignore=true

# 数据库连接池优化
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 30000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

### Nginx性能优化
```nginx
# nginx/nginx.conf
user nginx;
worker_processes auto;
worker_rlimit_nofile 65535;

events {
    worker_connections 4096;
    use epoll;
    multi_accept on;
}

http {
    charset utf-8;
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    server_tokens off;
    log_not_found off;
    types_hash_max_size 4096;
    client_max_body_size 50M;

    # MIME
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Logging
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log warn;

    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;

    # 缓存配置
    location ~* \.(css|js|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 7d;
        proxy_send_timeout 7d;
        proxy_read_timeout 7d;
    }
}
```