<template>
  <div id="app">
    <h1>{{ title }}</h1>
    <p>✅ Vue 3 + TypeScript + Element Plus + Pinia 框架运行正常</p>

    <div class="demo-section">
      <el-button type="primary" @click="handleLogin">
        <el-icon><User /></el-icon>
        {{ authStore.isAuthenticated ? '已登录' : '模拟登录' }}
      </el-button>

      <el-tag type="success">{{ status }}</el-tag>

      <el-button @click="appStore.toggleSidebar()">
        侧边栏：{{ appStore.sidebarCollapsed ? '收起' : '展开' }}
      </el-button>
    </div>

    <div v-if="authStore.isAuthenticated" class="user-info">
      <p>欢迎，{{ authStore.userDisplayName }}!</p>
      <el-button size="small" @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useAuthStore, useAppStore } from './stores'

const title = ref<string>('Codivio 前端框架')
const status = ref<string>('基础框架搭建完成')

// 使用 stores
const authStore = useAuthStore()
const appStore = useAppStore()

const handleLogin = () => {
  if (!authStore.isAuthenticated) {
    // 模拟登录
    authStore.setAuth('mock-token', {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      nickname: '测试用户',
    })
    ElMessage.success('登录成功！Pinia 状态管理正常工作！')
  }
}

const handleLogout = () => {
  authStore.clearAuth()
  ElMessage.info('已退出登录')
}
</script>

<style>
#app {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  text-align: center;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

h1 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

p {
  color: #27ae60;
  font-size: 1.2rem;
  margin-bottom: 2rem;
}

.demo-section {
  display: flex;
  gap: 1rem;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}

.user-info {
  margin-top: 2rem;
  padding: 1rem;
  background-color: #f5f7fa;
  border-radius: 8px;
}
</style>
