<template>
  <div class="register-container">
    <div class="register-form">
      <h2>注册 Codivio</h2>
      
      <el-form 
        ref="registerFormRef"
        :model="registerForm" 
        :rules="registerRules"
        label-width="80px"
        @keyup.enter="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入用户名"
            :disabled="isLoading"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="请输入邮箱地址"
            :disabled="isLoading"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input 
            v-model="registerForm.nickname" 
            placeholder="请输入昵称（可选）"
            :disabled="isLoading"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :disabled="isLoading"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :disabled="isLoading"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="isLoading"
            @click="handleRegister"
          >
            {{ isLoading ? '注册中...' : '注册' }}
          </el-button>
          <el-button @click="$router.push('/login')">
            已有账号？登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authAPI, type RegisterForm } from '@/api/auth'

const router = useRouter()

// 表单引用
const registerFormRef = ref<FormInstance>()

// 表单数据
const registerForm = reactive<RegisterForm>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

// 加载状态
const isLoading = ref(false)

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 处理注册逻辑
 */
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查输入信息')
    return
  }
  
  isLoading.value = true
  
  try {
    const response = await authAPI.register(registerForm)
    const userInfo = response.data.data
    
    ElMessage.success(`注册成功！欢迎 ${userInfo.nickname || userInfo.username}`)
    
    // 注册成功后跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1500)
    
  } catch (error: any) {
    console.error('注册失败:', error)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px 0;
}

.register-container::before {
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

.register-form {
  width: 450px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.3);
  margin: 20px;
}

.register-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
  font-size: 28px;
  font-weight: bold;
}

:deep(.el-button--primary) {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

:deep(.el-button:not(.el-button--primary)) {
  height: 40px;
  border-radius: 8px;
  font-weight: 500;
}

:deep(.el-input__inner) {
  border-radius: 8px;
  border: 1px solid #e0e6ed;
  transition: all 0.3s ease;
  height: 44px;
}

:deep(.el-input__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

:deep(.el-form-item__error) {
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-form {
    width: 100%;
    margin: 20px;
    padding: 30px 20px;
  }
  
  .register-container {
    padding: 20px;
  }
}
</style>