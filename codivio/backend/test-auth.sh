#!/bin/bash
# Auth功能测试脚本

BASE_URL="http://localhost:8080/api/v1"

echo "🚀 开始测试Codivio Auth功能..."
echo "=================================="

# 1. 健康检查
echo "📋 1. 健康检查"
curl -X GET "$BASE_URL/health" \
  -H "Content-Type: application/json" | jq .

echo -e "\n"

# 2. 用户注册
echo "📝 2. 用户注册"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@codivio.dev",
    "password": "test123456",
    "displayName": "测试用户"
  }')

echo $REGISTER_RESPONSE | jq .

echo -e "\n"

# 3. 用户登录
echo "🔐 3. 用户登录"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123456"
  }')

echo $LOGIN_RESPONSE | jq .

# 提取access token
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')
echo "Access Token: $ACCESS_TOKEN"

echo -e "\n"

# 4. 获取当前用户信息
echo "👤 4. 获取当前用户信息"
curl -X GET "$BASE_URL/users/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

echo -e "\n"

# 5. 更新用户信息
echo "✏️ 5. 更新用户信息"
curl -X PUT "$BASE_URL/users/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "displayName": "更新后的用户名",
    "bio": "这是我的个人简介"
  }' | jq .

echo -e "\n"

# 6. 检查用户名可用性
echo "🔍 6. 检查用户名可用性"
curl -X GET "$BASE_URL/auth/check-username?username=newuser" \
  -H "Content-Type: application/json" | jq .

echo -e "\n"

# 7. 认证测试
echo "🛡️ 7. 认证测试"
curl -X GET "$BASE_URL/health/auth-test" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

echo -e "\n"

# 8. 未授权访问测试
echo "❌ 8. 未授权访问测试"
curl -X GET "$BASE_URL/users/me" \
  -H "Content-Type: application/json" | jq .

echo -e "\n"

echo "✅ Auth功能测试完成！"