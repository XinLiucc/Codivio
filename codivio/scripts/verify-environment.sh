#!/bin/bash

echo "🔍 Codivio开发环境验证..."

# 检查Docker服务
echo "检查Docker服务..."
if docker compose -f docker-compose.dev.yml ps | grep -q "Up"; then
    echo "✅ Docker服务正常运行"
else
    echo "❌ Docker服务未正常运行"
    exit 1
fi

# 检查数据库连接
echo "检查MySQL连接..."
if mysql -h 127.0.0.1 -P 3306 -u codivio_dev -pcodivio123 -e "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ MySQL连接正常"
else
    echo "❌ MySQL连接失败"
fi

# 检查Redis连接
echo "检查Redis连接..."
if redis-cli -h 127.0.0.1 -p 6379 -a redis123 ping > /dev/null 2>&1; then
    echo "✅ Redis连接正常"
else
    echo "❌ Redis连接失败"
fi

# 检查后端健康状态
echo "检查后端服务..."
if curl -s http://localhost:8080/api/v1/actuator/health > /dev/null 2>&1; then
    echo "✅ 后端服务正常"
else
    echo "⚠️  后端服务未启动或有问题"
fi

# 检查前端服务
echo "检查前端服务..."
if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ 前端服务正常"
else
    echo "⚠️  前端服务未启动"
fi

echo "🎉 环境验证完成！"
