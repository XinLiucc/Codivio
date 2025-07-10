<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link to="/" class="flex items-center">
              <el-icon :size="24" color="#3b82f6" class="mr-2">
                <Monitor />
              </el-icon>
              <span class="text-xl font-bold text-gray-900">Codivio</span>
            </router-link>
          </div>
          <div class="flex items-center space-x-4">
            <el-button @click="$router.push('/')">首页</el-button>
            <el-dropdown @command="handleUserCommand">
              <span class="el-dropdown-link flex items-center cursor-pointer">
                <el-avatar :src="userStore.userAvatar" :size="32" class="mr-2" />
                {{ userStore.displayName }}
                <el-icon class="ml-1"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主要内容 -->
    <main class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">仪表板</h1>
        <p class="text-gray-600">管理您的项目和协作</p>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <el-card>
          <div class="flex items-center">
            <div class="flex-shrink-0 p-3 bg-blue-100 rounded-lg">
              <el-icon :size="24" color="#3b82f6">
                <FolderOpened />
              </el-icon>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">总项目数</div>
              <div class="text-2xl font-bold text-gray-900">12</div>
            </div>
          </div>
        </el-card>

        <el-card>
          <div class="flex items-center">
            <div class="flex-shrink-0 p-3 bg-green-100 rounded-lg">
              <el-icon :size="24" color="#10b981">
                <UserFilled />
              </el-icon>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">协作项目</div>
              <div class="text-2xl font-bold text-gray-900">8</div>
            </div>
          </div>
        </el-card>

        <el-card>
          <div class="flex items-center">
            <div class="flex-shrink-0 p-3 bg-yellow-100 rounded-lg">
              <el-icon :size="24" color="#f59e0b">
                <Timer />
              </el-icon>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">本周活跃</div>
              <div class="text-2xl font-bold text-gray-900">24h</div>
            </div>
          </div>
        </el-card>

        <el-card>
          <div class="flex items-center">
            <div class="flex-shrink-0 p-3 bg-purple-100 rounded-lg">
              <el-icon :size="24" color="#8b5cf6">
                <Star />
              </el-icon>
            </div>
            <div class="ml-4">
              <div class="text-sm text-gray-500">获得Stars</div>
              <div class="text-2xl font-bold text-gray-900">156</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 项目列表 -->
      <el-card>
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-semibold">我的项目</span>
            <el-button type="primary" @click="createProject">
              <el-icon class="mr-1"><Plus /></el-icon>
              新建项目
            </el-button>
          </div>
        </template>
        
        <div class="space-y-4">
          <div class="text-center py-12 text-gray-500">
            <el-icon :size="64" color="#d1d5db" class="mb-4">
              <FolderOpened />
            </el-icon>
            <div class="text-lg mb-2">暂无项目</div>
            <div class="text-sm">开始创建您的第一个项目吧！</div>
          </div>
        </div>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { 
  Monitor, ArrowDown, FolderOpened, UserFilled, Timer, Star, Plus 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const userStore = useUserStore()

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      userStore.logout()
      break
  }
}

const createProject = () => {
  ElMessage.info('创建项目功能正在开发中...')
}
</script>

<style scoped>
.el-dropdown-link {
  color: #409eff;
}
</style>