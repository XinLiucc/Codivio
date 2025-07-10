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
        <p class="text-gray-600 mt-2">加入协作开发社区</p>
      </div>

      <!-- 注册表单 -->
      <div class="bg-white rounded-lg shadow-xl p-8">
        <h2 class="text-2xl font-semibold text-gray-900 mb-6 text-center">创建账号</h2>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          @submit.prevent="handleRegister"
          size="large"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
              @blur="checkUsernameAvailability"
            >
              <template #suffix>
                <el-icon v-if="usernameChecking" class="is-loading">
                  <Loading />
                </el-icon>
                <el-icon v-else-if="usernameAvailable === true" color="#67c23a">
                  <Check />
                </el-icon>
                <el-icon v-else-if="usernameAvailable === false" color="#f56c6c">
                  <Close />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱地址"
              prefix-icon="Message"
              clearable
              @blur="checkEmailAvailability"
            >
              <template #suffix>
                <el-icon v-if="emailChecking" class="is-loading">
                  <Loading />
                </el-icon>
                <el-icon v-else-if="emailAvailable === true" color="#67c23a">
                  <Check />
                </el-icon>
                <el-icon v-else-if="emailAvailable === false" color="#f56c6c">
                  <Close />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="displayName">
            <el-input
              v-model="registerForm.displayName"
              placeholder="请输入显示名称（可选）"
              prefix-icon="Avatar"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item prop="inviteCode">
            <el-input
              v-model="registerForm.inviteCode"
              placeholder="邀请码（可选）"
              prefix-icon="Ticket"
              clearable
            />
          </el-form-item>

          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.agreement">
              我已阅读并同意
              <el-link type="primary" @click="showTerms">用户协议</el-link>
              和
              <el-link type="primary" @click="showPrivacy">隐私政策</el-link>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="w-full"
              :loading="userStore.loading"
              :disabled="!canRegister"
              @click="handleRegister"
            >
              <span v-if="!userStore.loading">创建账号</span>
              <span v-else>注册中...</span>
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

        <!-- 第三方注册 -->
        <div class="space-y-3">
          <el-button class="w-full" @click="handleGithubRegister">
            <el-icon class="mr-2">
              <svg viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
            </el-icon>
            使用 GitHub 注册
          </el-button>
        </div>

        <!-- 登录链接 -->
        <div class="mt-6 text-center">
          <span class="text-gray-600">已有账号？</span>
          <router-link 
            to="/login" 
            class="text-blue-600 hover:text-blue-700 font-medium ml-1"
          >
            立即登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { 
  Monitor, User, Message, Avatar, Lock, Ticket, 
  Loading, Check, Close 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authAPI, type RegisterRequest } from '@/api/auth'

const userStore = useUserStore()

// 表单引用
const registerFormRef = ref<FormInstance>()

// 表单数据
const registerForm = reactive<RegisterRequest>({
  username: '',
  email: '',
  password: '',
  displayName: '',
  inviteCode: '',
  confirmPassword: '',
  agreement: false
})

// 确认密码
const confirmPassword = ref('')

// 协议同意
const agreement = ref(false)

// 用户名可用性检查
const usernameChecking = ref(false)
const usernameAvailable = ref<boolean | null>(null)

// 邮箱可用性检查
const emailChecking = ref(false)
const emailAvailable = ref<boolean | null>(null)

// 自定义验证器
const validateUsername = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入用户名'))
    return
  }
  
  if (value.length < 3 || value.length > 50) {
    callback(new Error('用户名长度应在 3 到 50 个字符'))
    return
  }
  
  // 检查用户名格式
  const usernameRegex = /^[a-zA-Z0-9_-]+$/
  if (!usernameRegex.test(value)) {
    callback(new Error('用户名只能包含字母、数字、下划线和连字符'))
    return
  }
  
  if (usernameAvailable.value === false) {
    callback(new Error('用户名已被占用'))
    return
  }
  
  callback()
}

const validateEmail = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入邮箱地址'))
    return
  }
  
  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'))
    return
  }
  
  if (emailAvailable.value === false) {
    callback(new Error('邮箱已被注册'))
    return
  }
  
  callback()
}

const validatePassword = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }
  
  if (value.length < 6 || value.length > 50) {
    callback(new Error('密码长度应在 6 到 50 个字符'))
    return
  }
  
  // 密码强度检查
  const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/
  if (!strongRegex.test(value)) {
    callback(new Error('密码应包含大小写字母和数字'))
    return
  }
  
  callback()
}

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请确认密码'))
    return
  }
  
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  
  callback()
}

const validateAgreement = (rule: any, value: boolean, callback: any) => {
  if (!value) {
    callback(new Error('请先同意用户协议和隐私政策'))
    return
  }
  
  callback()
}

// 表单验证规则
const registerRules: FormRules = {
  username: [{ validator: validateUsername, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  agreement: [{ validator: validateAgreement, trigger: 'change' }]
}

// 是否可以注册
const canRegister = computed(() => {
  return registerForm.agreement && 
         usernameAvailable.value === true && 
         emailAvailable.value === true &&
         registerForm.username &&
         registerForm.email &&
         registerForm.password &&
         registerForm.confirmPassword === registerForm.password
})

// 检查用户名可用性
const checkUsernameAvailability = async () => {
  if (!registerForm.username || registerForm.username.length < 3) {
    usernameAvailable.value = null
    return
  }
  
  try {
    usernameChecking.value = true
    const response = await authAPI.checkUsername(registerForm.username)
    usernameAvailable.value = response.data
    
    if (!response.data) {
      ElMessage.warning('用户名已被占用')
    }
  } catch (error) {
    console.error('Check username failed:', error)
    usernameAvailable.value = null
  } finally {
    usernameChecking.value = false
  }
}

// 检查邮箱可用性
const checkEmailAvailability = async () => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!registerForm.email || !emailRegex.test(registerForm.email)) {
    emailAvailable.value = null
    return
  }
  
  try {
    emailChecking.value = true
    const response = await authAPI.checkEmail(registerForm.email)
    emailAvailable.value = response.data
    
    if (!response.data) {
      ElMessage.warning('邮箱已被注册')
    }
  } catch (error) {
    console.error('Check email failed:', error)
    emailAvailable.value = null
  } finally {
    emailChecking.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    // 验证表单
    await registerFormRef.value.validate()
    
    // 最终检查
    if (!canRegister.value) {
      ElMessage.error('请完善注册信息')
      return
    }
    
    // 执行注册
    await userStore.register(registerForm)
    
  } catch (error) {
    console.error('Register error:', error)
  }
}

// 显示用户协议
const showTerms = () => {
  ElMessage.info('用户协议功能正在开发中...')
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessage.info('隐私政策功能正在开发中...')
}

// GitHub注册
const handleGithubRegister = () => {
  ElMessage.info('GitHub注册功能正在开发中...')
}
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

/* 加载动画 */
.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 640px) {
  .max-w-md {
    max-width: 100%;
    margin: 0 16px;
  }
}
</style>