<template>
  <div class="login-container">
    <div class="login-form">
      <div class="login-header">
        <h2>登录 Codivio</h2>
        <p class="login-subtitle">欢迎回来，开始你的代码协作之旅</p>
      </div>
      
      <!-- 表单验证的关键：ref引用和rules规则 -->
      <el-form 
        ref="loginFormRef"
        :model="loginForm" 
        :rules="loginRules"
        label-width="0"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="loginId">
          <el-input 
            v-model="loginForm.loginId" 
            placeholder="请输入用户名或邮箱"
            prefix-icon="User"
            :disabled="isLoading"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            :disabled="isLoading"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="isLoading"
            :disabled="isLoading"
            class="login-button"
            @click="handleLogin"
          >
            {{ isLoading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        
        <el-form-item class="form-links">
          <el-link type="primary" @click="$router.push('/register')">
            还没有账号？立即注册
          </el-link>
          <el-link type="info" @click="$router.push('/')">
            返回首页
          </el-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authAPI, type LoginForm } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用 - 用于调用表单验证方法
const loginFormRef = ref<FormInstance>()

// 表单数据
const loginForm = reactive<LoginForm>({
  loginId: '',  // 支持用户名或邮箱
  password: ''
})

// 使用store中的加载状态
const isLoading = computed(() => authStore.isLoading)

// 表单验证规则
// 为什么需要验证规则？
// 1. 用户体验 - 实时提示输入错误
// 2. 数据质量 - 确保发送到后端的数据格式正确
// 3. 减少网络请求 - 前端验证通过才发送请求
const loginRules: FormRules = {
  loginId: [
    {
      required: true,
      message: '请输入用户名或邮箱',
      trigger: 'blur'
    },
    {
      min: 3,
      max: 50,
      message: '长度在 3 到 50 个字符',
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    },
    {
      min: 6,
      message: '密码长度不能少于 6 位',
      trigger: 'blur'
    }
  ]
}

/**
 * 处理登录逻辑
 * 为什么要这样设计登录流程？
 * 1. 表单验证优先 - 确保数据格式正确
 * 2. 加载状态管理 - 防止重复提交，提供用户反馈
 * 3. 错误处理 - 统一的错误提示（HTTP拦截器已处理）
 * 4. 状态管理 - 登录成功后更新全局认证状态
 * 5. 路由跳转 - 登录成功后跳转到合适页面
 */
const handleLogin = async () => {
  // 步骤1: 表单验证
  if (!loginFormRef.value) return
  
  try {
    // 验证表单数据
    await loginFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查输入信息')
    return
  }
  
  // 步骤2: 调用store的登录方法
  // 所有登录逻辑（API调用、状态管理、路由跳转）都在store中处理
  try {
    await authStore.login(loginForm)
  } catch (error: any) {
    // 错误处理已经在store的login方法和HTTP拦截器中处理
    console.error('登录失败:', error)
  }
}

/**
 * 为什么使用这种错误处理方式？
 * 
 * 1. 分层处理：
 *    - HTTP拦截器：统一处理网络错误和业务错误
 *    - 组件层面：处理特定的业务逻辑
 * 
 * 2. 用户体验：
 *    - 加载状态提供视觉反馈
 *    - 按钮禁用防止重复提交
 *    - 成功后的欢迎信息增加亲切感
 * 
 * 3. 代码维护：
 *    - 错误处理逻辑集中，不需要在每个API调用处重复
 *    - 类型安全的API调用和响应处理
 */
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="2" fill="%23ffffff" opacity="0.1"/></svg>') repeat;
  animation: float 20s linear infinite;
}

@keyframes float {
  0% { transform: translateY(0px) rotate(0deg); }
  100% { transform: translateY(-100px) rotate(360deg); }
}

.login-form {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 28px;
  font-weight: bold;
}

.login-subtitle {
  color: #606266;
  margin: 0;
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.form-links {
  margin-top: 8px;
}

.form-links :deep(.el-form-item__content) {
  display: flex;
  justify-content: space-between;
}

:deep(.el-input__inner) {
  border-radius: 8px;
  border: 1px solid #e0e6ed;
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.2);
}

:deep(.el-input__prefix) {
  color: #909399;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-link) {
  font-weight: 500;
}
</style>