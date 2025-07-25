<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center px-4">
    <div class="max-w-md w-full">
      <!-- Logo和标题 -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-full mb-4">
          <el-icon :size="32" color="white">
            <Monitor />
          </el-icon>
        </div>
        <h1 class="text-3xl font-bold text-gray-900">Codivio</h1>
        <p class="text-gray-600 mt-2">分布式实时协作开发平台</p>
      </div>

      <!-- 登录表单 -->
      <div class="bg-white rounded-lg shadow-xl p-8">
        <h2 class="text-2xl font-semibold text-gray-900 mb-6 text-center">登录账号</h2>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          @submit.prevent="handleLogin"
          size="large"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名或邮箱"
              prefix-icon="User"
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <div class="w-full flex items-center justify-between">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link type="primary" @click="handleForgotPassword">忘记密码？</el-link>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="w-full"
              :loading="userStore.loading"
              @click="handleLogin"
            >
              <span v-if="!userStore.loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 分割线 -->
        <div class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-300"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-2 bg-white text-gray-500">或者</span>
          </div>
        </div>

        <!-- 第三方登录 -->
        <div class="space-y-3">
          <el-button class="w-full" @click="handleGithubLogin">
            <el-icon class="mr-2">
              <svg viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
            </el-icon>
            使用 GitHub 登录
          </el-button>
          
          <el-button class="w-full" @click="handleGoogleLogin">
            <el-icon class="mr-2">
              <svg viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
            </el-icon>
            使用 Google 登录
          </el-button>
        </div>

        <!-- 注册链接 -->
        <div class="mt-6 text-center">
          <span class="text-gray-600">还没有账号？</span>
          <router-link 
            to="/register" 
            class="text-blue-600 hover:text-blue-700 font-medium ml-1"
          >
            立即注册
          </router-link>
        </div>
      </div>

      <!-- 帮助信息 -->
      <div class="mt-8 text-center text-sm text-gray-500">
        <p>遇到问题？查看 
          <el-link type="primary" href="#" target="_blank">帮助文档</el-link> 
          或 
          <el-link type="primary" href="#" target="_blank">联系支持</el-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Monitor, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/api/auth'

const userStore = useUserStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 表单数据
const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

// 记住我选项
const rememberMe = ref(false)

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度应在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度应在 6 到 50 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    // 验证表单
    await loginFormRef.value.validate()
    
    // 执行登录
    await userStore.login(loginForm)
    
  } catch (error) {
    console.error('Login error:', error)
  }
}

// 处理忘记密码
const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能正在开发中...')
}

// GitHub登录
const handleGithubLogin = () => {
  ElMessage.info('GitHub登录功能正在开发中...')
}

// Google登录
const handleGoogleLogin = () => {
  ElMessage.info('Google登录功能正在开发中...')
}

// 页面挂载时的操作
onMounted(() => {
  // 如果是开发环境，可以预填充测试数据
  if (import.meta.env.DEV) {
    loginForm.username = 'XinLiucc'
    loginForm.password = 'Asdjkl1314'
  }
})
</script>

<style scoped>
.el-button {
  height: 44px;
}

.el-input {
  height: 44px;
}

:deep(.el-input__inner) {
  height: 44px;
  line-height: 44px;
}

/* 美化checkbox */
:deep(.el-checkbox__label) {
  color: #6b7280;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .max-w-md {
    max-width: 100%;
    margin: 0 16px;
  }
}
</style>