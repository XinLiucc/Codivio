<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="flex items-center">
              <el-icon :size="24" color="#3b82f6" class="mr-2">
                <Monitor />
              </el-icon>
              <span class="text-xl font-bold text-gray-900">Codivio</span>
            </div>
          </div>
          <div class="flex items-center space-x-4">
            <el-button @click="$router.push('/dashboard')">仪表板</el-button>
            <el-button type="primary" @click="createProject">新建项目</el-button>
            <el-dropdown @command="handleUserCommand">
              <span class="el-dropdown-link flex items-center cursor-pointer">
                <el-avatar :src="userStore.userAvatar" :size="32" class="mr-2" />
                {{ userStore.displayName }}
                <el-icon class="ml-1"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="settings">设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主要内容 -->
    <main class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 欢迎区域 -->
      <div class="bg-white rounded-lg shadow p-6 mb-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">
              欢迎回来，{{ userStore.displayName }}！
            </h1>
            <p class="text-gray-600 mt-1">
              开始您的协作开发之旅
            </p>
          </div>
          <div class="text-right">
            <div class="text-sm text-gray-500">
              今天是 {{ currentDate }}
            </div>
          </div>
        </div>
      </div>

      <!-- 快速操作 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <el-card class="hover:shadow-lg transition-shadow cursor-pointer" @click="createProject">
          <div class="text-center">
            <el-icon :size="48" color="#3b82f6" class="mb-4">
              <Plus />
            </el-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">新建项目</h3>
            <p class="text-gray-600">创建一个新的协作项目</p>
          </div>
        </el-card>

        <el-card class="hover:shadow-lg transition-shadow cursor-pointer" @click="joinProject">
          <div class="text-center">
            <el-icon :size="48" color="#10b981" class="mb-4">
              <UserFilled />
            </el-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">加入项目</h3>
            <p class="text-gray-600">通过邀请码加入现有项目</p>
          </div>
        </el-card>

        <el-card class="hover:shadow-lg transition-shadow cursor-pointer" @click="exploreProjects">
          <div class="text-center">
            <el-icon :size="48" color="#f59e0b" class="mb-4">
              <Search />
            </el-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">探索项目</h3>
            <p class="text-gray-600">发现有趣的开源项目</p>
          </div>
        </el-card>
      </div>

      <!-- 最近项目 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">最近项目</h2>
          <el-button text @click="$router.push('/dashboard')">查看全部</el-button>
        </div>
        <div class="space-y-4">
          <div 
            v-for="project in recentProjects" 
            :key="project.id"
            class="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer"
            @click="openProject(project)"
          >
            <div class="flex-shrink-0 w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center mr-4">
              <el-icon :size="20" color="#3b82f6">
                <FolderOpened />
              </el-icon>
            </div>
            <div class="flex-1">
              <h3 class="text-sm font-medium text-gray-900">{{ project.name }}</h3>
              <p class="text-sm text-gray-500">{{ project.description }}</p>
            </div>
            <div class="text-right">
              <div class="text-xs text-gray-500">{{ project.language }}</div>
              <div class="text-xs text-gray-400">{{ project.updatedAt }}</div>
            </div>
          </div>
          
          <div v-if="recentProjects.length === 0" class="text-center py-8 text-gray-500">
            暂无项目，开始创建您的第一个项目吧！
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Monitor, ArrowDown, Plus, UserFilled, Search, FolderOpened 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const userStore = useUserStore()

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 最近项目数据
const recentProjects = ref([
  {
    id: 1,
    name: 'Hello World 项目',
    description: '第一个演示项目，包含基础的前端和后端代码',
    language: 'JavaScript',
    updatedAt: '2小时前'
  },
  {
    id: 2,
    name: 'Vue 待办事项应用',
    description: '使用Vue.js开发的待办事项管理应用',
    language: 'Vue',
    updatedAt: '1天前'
  },
  {
    id: 3,
    name: '在线聊天室',
    description: '基于WebSocket的实时聊天应用',
    language: 'TypeScript',
    updatedAt: '3天前'
  }
])

// 处理用户菜单命令
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('设置功能正在开发中...')
      break
    case 'logout':
      userStore.logout()
      break
  }
}

// 创建项目
const createProject = () => {
  ElMessage.info('创建项目功能正在开发中...')
}

// 加入项目
const joinProject = () => {
  ElMessage.info('加入项目功能正在开发中...')
}

// 探索项目
const exploreProjects = () => {
  ElMessage.info('探索项目功能正在开发中...')
}

// 打开项目
const openProject = (project: any) => {
  ElMessage.info(`打开项目: ${project.name}`)
}

onMounted(() => {
  // 页面加载时可以获取最近项目数据
})
</script>

<style scoped>
.el-dropdown-link {
  color: #409eff;
}

:deep(.el-card__body) {
  padding: 24px;
}
</style>