<template>
  <div class="main-layout">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="8" fill="#409eff"/>
            <path d="M8 10l4 4-4 4M14 18h6" stroke="white" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span class="logo-text">Codivio</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon class="nav-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">
            {{ userInitial }}
          </div>
          <div class="user-meta">
            <div class="user-name">{{ userName }}</div>
            <div class="user-role">已登录</div>
          </div>
        </div>
        <el-tooltip content="退出登录" placement="right">
          <button class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
          </button>
        </el-tooltip>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Odometer, Folder, User, Setting, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const navItems = [
  { path: '/dashboard', label: '仪表盘', icon: Odometer },
  { path: '/projects', label: '项目管理', icon: Folder },
  { path: '/profile', label: '个人信息', icon: User },
  { path: '/settings', label: '偏好设置', icon: Setting },
]

const isActive = (path: string) => {
  if (path === '/projects') {
    return route.path === '/projects' || route.path.startsWith('/projects/')
  }
  return route.path === path
}

const userName = computed(() => authStore.user?.username || authStore.user?.nickname || '用户')
const userInitial = computed(() => (userName.value).charAt(0).toUpperCase())

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    authStore.clearAuth()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 取消
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background: var(--app-bg);
}

/* 侧边栏 */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
  transition: background 0.2s, border-color 0.2s;
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid var(--sidebar-logo-border);
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--logo-text-color);
  letter-spacing: 0.3px;
}

/* 导航 */
.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  color: var(--nav-text);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.15s;
  cursor: pointer;
}

.nav-item:hover {
  background: var(--nav-hover-bg);
  color: var(--nav-active-text);
}

.nav-item.active {
  background: var(--nav-active-bg);
  color: var(--nav-active-text);
}

.nav-item.active .nav-icon {
  color: var(--nav-active-text);
}

.nav-icon {
  font-size: 17px;
  flex-shrink: 0;
}

.nav-label {
  flex: 1;
}

/* 底部用户区 */
.sidebar-footer {
  padding: 12px 14px;
  border-top: 1px solid var(--sidebar-footer-border);
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #6ec6ff);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-meta {
  min-width: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--user-name-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 11px;
  color: var(--user-role-color);
}

.logout-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 6px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.15s;
  flex-shrink: 0;
}

.logout-btn:hover {
  background: #fff1f0;
  color: #f56c6c;
}

/* 主内容区 */
.main-content {
  margin-left: 220px;
  flex: 1;
  min-height: 100vh;
  overflow-y: auto;
  background: var(--app-bg);
  transition: background 0.2s;
}
</style>
