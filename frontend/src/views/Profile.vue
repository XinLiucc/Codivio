<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人中心</h2>
      <p class="profile-subtitle">管理您的个人信息和账户设置</p>
    </div>

    <el-row :gutter="24">
      <!-- 个人信息卡片 -->
      <el-col :span="16">
        <el-card title="个人信息">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button v-if="!isEditing" type="primary" @click="startEdit">
                编辑
              </el-button>
              <div v-else>
                <el-button @click="cancelEdit">取消</el-button>
                <el-button type="primary" :loading="isUpdating" @click="saveProfile">
                  保存
                </el-button>
              </div>
            </div>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            :disabled="!isEditing"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
              <div class="form-tip">用户名不能修改</div>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="头像">
              <AvatarUpload 
                v-if="isEditing"
                :avatar-url="profileForm.avatarUrl"
                :size="80"
                @success="handleAvatarSuccess"
                @error="handleAvatarError"
              />
              <el-avatar 
                v-else 
                :size="80" 
                :src="profileForm.avatarUrl" 
                :icon="UserFilled"
              >
                {{ user?.nickname?.charAt(0) || user?.username?.charAt(0) }}
              </el-avatar>
            </el-form-item>

            <el-form-item label="注册时间">
              <span class="info-text">{{ formatDate(user?.createdAt) }}</span>
            </el-form-item>

            <el-form-item label="最后登录">
              <span class="info-text">{{ formatDate(user?.lastLoginAt) }}</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="8">
        <el-card title="快捷操作">
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/dashboard')">
              <el-icon><House /></el-icon>
              返回仪表板
            </el-button>
            
            <el-button @click="showPasswordDialog = true">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-button>
            
            <el-button type="danger" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-card>

        <el-card title="账户统计" class="stats-card">
          <div class="stats-item">
            <span class="stats-label">账户状态:</span>
            <el-tag :type="user?.status === 1 ? 'success' : 'danger'">
              {{ user?.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="isChangingPassword" @click="changePassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { UserFilled, House, Lock, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { authAPI, type UpdateProfileForm, type ChangePasswordForm } from '@/api/auth'
import AvatarUpload from '@/components/AvatarUpload.vue'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const user = computed(() => authStore.user)

// 编辑状态
const isEditing = ref(false)
const isUpdating = ref(false)

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 个人信息表单
const profileForm = reactive({
  username: '',
  email: '',
  nickname: '',
  avatarUrl: ''
})

// 原始数据备份
const originalProfile = reactive({
  username: '',
  email: '',
  nickname: '',
  avatarUrl: ''
})

// 密码修改
const showPasswordDialog = ref(false)
const isChangingPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const profileRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  nickname: [
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 初始化个人信息
const initProfile = () => {
  if (user.value) {
    Object.assign(profileForm, {
      username: user.value.username,
      email: user.value.email,
      nickname: user.value.nickname || '',
      avatarUrl: user.value.avatarUrl || ''
    })
    
    // 备份原始数据
    Object.assign(originalProfile, profileForm)
  }
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  initProfile() // 重新初始化，防止数据污染
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  // 恢复原始数据
  Object.assign(profileForm, originalProfile)
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查输入信息')
    return
  }

  isUpdating.value = true
  
  try {
    // 准备更新数据（只包含可修改的字段）
    const updateData: UpdateProfileForm = {
      email: profileForm.email,
      nickname: profileForm.nickname || undefined,
      avatarUrl: profileForm.avatarUrl || undefined
    }
    
    // 调用API更新用户信息
    const response = await authAPI.updateProfile(updateData)
    const updatedUser = response.data.data
    
    // 更新store中的用户信息
    authStore.updateUser(updatedUser)
    
    // 更新本地表单数据
    Object.assign(profileForm, {
      username: updatedUser.username,
      email: updatedUser.email,
      nickname: updatedUser.nickname || '',
      avatarUrl: updatedUser.avatarUrl || ''
    })
    
    // 更新备份数据
    Object.assign(originalProfile, profileForm)
    
    ElMessage.success('个人信息更新成功')
    isEditing.value = false
    
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
    console.error('Profile update error:', error)
  } finally {
    isUpdating.value = false
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return
  }

  isChangingPassword.value = true
  
  try {
    // 准备密码数据
    const passwordData: ChangePasswordForm = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    }
    
    // 调用API修改密码
    await authAPI.changePassword(passwordData)
    
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false
    
    // 重置密码表单
    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 密码修改后需要重新登录
    setTimeout(() => {
      authStore.logout()
    }, 1500)
    
  } catch (error: any) {
    ElMessage.error(error.message || '修改密码失败')
    console.error('Change password error:', error)
  } finally {
    isChangingPassword.value = false
  }
}

// 头像上传成功处理
const handleAvatarSuccess = (avatarUrl: string) => {
  profileForm.avatarUrl = avatarUrl
  ElMessage.success('头像上传成功')
}

// 头像上传失败处理
const handleAvatarError = (error: Error) => {
  console.error('Avatar upload error:', error)
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await authStore.logout()
  } catch (error) {
    // 用户取消操作
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '暂无'
  
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时初始化
onMounted(() => {
  if (!user.value) {
    // 如果没有用户信息，尝试获取
    authStore.fetchCurrentUser().catch(() => {
      ElMessage.error('获取用户信息失败')
      router.push('/login')
    })
  } else {
    initProfile()
  }
})
</script>

<style scoped>
.profile-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 24px;
}

.profile-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.profile-subtitle {
  color: #606266;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-btn {
  padding: 0;
  font-size: 12px;
}

.info-text {
  color: #606266;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions .el-button {
  justify-content: flex-start;
}

.stats-card {
  margin-top: 16px;
}

.stats-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.stats-label {
  color: #606266;
  font-size: 14px;
}
</style>