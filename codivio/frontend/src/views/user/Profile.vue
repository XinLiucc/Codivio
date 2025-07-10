<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <div class="bg-white shadow">
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
            <el-button @click="$router.go(-1)">返回</el-button>
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link flex items-center">
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
    </div>

    <!-- 主要内容 -->
    <div class="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <div class="bg-white shadow rounded-lg">
        <!-- 用户头部信息 -->
        <div class="px-6 py-8 border-b border-gray-200">
          <div class="flex items-center">
            <div class="relative">
              <el-avatar :src="userStore.userAvatar" :size="80" class="mr-6" />
              <el-button 
                circle 
                type="primary" 
                :icon="Camera" 
                size="small"
                class="absolute bottom-0 right-4"
                @click="handleAvatarUpload"
              />
            </div>
            <div class="flex-1">
              <h1 class="text-2xl font-bold text-gray-900">{{ userStore.displayName }}</h1>
              <p class="text-gray-600">@{{ userStore.user?.username }}</p>
              <p class="text-gray-500 mt-1">{{ userStore.user?.email }}</p>
              <div class="mt-3 flex items-center space-x-4">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <el-icon class="mr-1" :size="12">
                    <Check />
                  </el-icon>
                  已验证
                </span>
                <span class="text-sm text-gray-500">
                  加入时间：{{ formatDate(userStore.user?.createdAt) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 标签页 -->
        <el-tabs v-model="activeTab" class="px-6">
          <el-tab-pane label="基本信息" name="basic">
            <div class="py-6">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="120px"
                size="large"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input 
                    v-model="profileForm.username" 
                    disabled
                    placeholder="用户名不可修改"
                  />
                  <div class="text-xs text-gray-500 mt-1">
                    用户名创建后不可修改
                  </div>
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input 
                    v-model="profileForm.email" 
                    disabled
                    placeholder="邮箱不可修改"
                  />
                  <div class="text-xs text-gray-500 mt-1">
                    如需修改邮箱，请联系客服
                  </div>
                </el-form-item>

                <el-form-item label="显示名称" prop="displayName">
                  <el-input 
                    v-model="profileForm.displayName" 
                    placeholder="请输入显示名称"
                    maxlength="100"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="个人简介" prop="bio">
                  <el-input 
                    v-model="profileForm.bio" 
                    type="textarea" 
                    :rows="4"
                    placeholder="介绍一下自己..."
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item label="头像链接" prop="avatarUrl">
                  <el-input 
                    v-model="profileForm.avatarUrl" 
                    placeholder="输入头像图片链接"
                  />
                  <div class="text-xs text-gray-500 mt-1">
                    建议使用 https:// 开头的图片链接
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="handleUpdateProfile"
                    :loading="userStore.loading"
                  >
                    保存修改
                  </el-button>
                  <el-button @click="resetProfileForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="安全设置" name="security">
            <div class="py-6">
              <div class="space-y-6">
                <!-- 修改密码 -->
                <el-card class="shadow-sm">
                  <template #header>
                    <div class="flex items-center">
                      <el-icon class="mr-2" color="#f56c6c">
                        <Lock />
                      </el-icon>
                      修改密码
                    </div>
                  </template>
                  <el-form
                    ref="passwordFormRef"
                    :model="passwordForm"
                    :rules="passwordRules"
                    label-width="120px"
                    size="large"
                  >
                    <el-form-item label="当前密码" prop="currentPassword">
                      <el-input 
                        v-model="passwordForm.currentPassword" 
                        type="password"
                        placeholder="请输入当前密码"
                        show-password
                      />
                    </el-form-item>

                    <el-form-item label="新密码" prop="newPassword">
                      <el-input 
                        v-model="passwordForm.newPassword" 
                        type="password"
                        placeholder="请输入新密码"
                        show-password
                      />
                    </el-form-item>

                    <el-form-item label="确认密码" prop="confirmPassword">
                      <el-input 
                        v-model="passwordForm.confirmPassword" 
                        type="password"
                        placeholder="请确认新密码"
                        show-password
                      />
                    </el-form-item>

                    <el-form-item>
                      <el-button type="primary" @click="handleChangePassword">
                        修改密码
                      </el-button>
                    </el-form-item>
                  </el-form>
                </el-card>

                <!-- 登录设备管理 -->
                <el-card class="shadow-sm">
                  <template #header>
                    <div class="flex items-center">
                      <el-icon class="mr-2" color="#409eff">
                        <Monitor />
                      </el-icon>
                      登录设备管理
                    </div>
                  </template>
                  <div class="text-gray-500">
                    <p>此功能正在开发中...</p>
                    <p class="text-sm mt-1">您可以查看和管理所有登录设备</p>
                  </div>
                </el-card>

                <!-- 两步验证 -->
                <el-card class="shadow-sm">
                  <template #header>
                    <div class="flex items-center">
                      <el-icon class="mr-2" color="#67c23a">
                        <Key />
                      </el-icon>
                      两步验证
                    </div>
                  </template>
                  <div class="text-gray-500">
                    <p>此功能正在开发中...</p>
                    <p class="text-sm mt-1">启用两步验证以增强账户安全性</p>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="账户设置" name="account">
            <div class="py-6">
              <div class="space-y-6">
                <!-- 隐私设置 -->
                <el-card class="shadow-sm">
                  <template #header>
                    <div class="flex items-center">
                      <el-icon class="mr-2" color="#909399">
                        <View />
                      </el-icon>
                      隐私设置
                    </div>
                  </template>
                  <div class="space-y-4">
                    <div class="flex items-center justify-between">
                      <div>
                        <div class="font-medium">公开个人资料</div>
                        <div class="text-sm text-gray-500">其他用户可以查看您的基本信息</div>
                      </div>
                      <el-switch v-model="privacySettings.profilePublic" />
                    </div>
                    <div class="flex items-center justify-between">
                      <div>
                        <div class="font-medium">显示邮箱</div>
                        <div class="text-sm text-gray-500">在个人资料中显示邮箱地址</div>
                      </div>
                      <el-switch v-model="privacySettings.showEmail" />
                    </div>
                    <div class="flex items-center justify-between">
                      <div>
                        <div class="font-medium">允许项目邀请</div>
                        <div class="text-sm text-gray-500">其他用户可以邀请您参与项目</div>
                      </div>
                      <el-switch v-model="privacySettings.allowInvites" />
                    </div>
                  </div>
                </el-card>

                <!-- 危险操作 -->
                <el-card class="shadow-sm">
                  <template #header>
                    <div class="flex items-center">
                      <el-icon class="mr-2" color="#f56c6c">
                        <Warning />
                      </el-icon>
                      危险操作
                    </div>
                  </template>
                  <div class="space-y-4">
                    <div class="p-4 bg-red-50 rounded-lg border border-red-200">
                      <div class="flex items-center justify-between">
                        <div>
                          <div class="font-medium text-red-800">删除账户</div>
                          <div class="text-sm text-red-600">永久删除您的账户和所有数据</div>
                        </div>
                        <el-button type="danger" @click="handleDeleteAccount">
                          删除账户
                        </el-button>
                      </div>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { 
  Monitor, Camera, Check, Lock, Key, View, Warning, ArrowDown 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const userStore = useUserStore()

// 当前激活的标签页
const activeTab = ref('basic')

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 个人资料表单
const profileForm = reactive({
  username: '',
  email: '',
  displayName: '',
  bio: '',
  avatarUrl: ''
})

// 密码修改表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 隐私设置
const privacySettings = reactive({
  profilePublic: true,
  showEmail: false,
  allowInvites: true
})

// 个人资料验证规则
const profileRules: FormRules = {
  displayName: [
    { max: 100, message: '显示名称不能超过100个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 500, message: '个人简介不能超过500个字符', trigger: 'blur' }
  ],
  avatarUrl: [
    { 
      pattern: /^https?:\/\/.+/, 
      message: '请输入有效的URL地址', 
      trigger: 'blur' 
    }
  ]
}

// 密码修改验证规则
const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度应在 6 到 50 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
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

// 初始化表单数据
const initFormData = () => {
  if (userStore.user) {
    profileForm.username = userStore.user.username
    profileForm.email = userStore.user.email
    profileForm.displayName = userStore.user.displayName || ''
    profileForm.bio = userStore.user.bio || ''
    profileForm.avatarUrl = userStore.user.avatarUrl || ''
  }
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 处理更新个人资料
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    
    const updateData = {
      displayName: profileForm.displayName,
      bio: profileForm.bio,
      avatarUrl: profileForm.avatarUrl || undefined
    }
    
    await userStore.updateUserInfo(updateData)
    
  } catch (error) {
    console.error('Update profile error:', error)
  }
}

// 重置个人资料表单
const resetProfileForm = () => {
  initFormData()
  ElMessage.info('已重置为原始数据')
}

// 处理头像上传
const handleAvatarUpload = () => {
  ElMessage.info('头像上传功能正在开发中...')
}

// 处理修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    
    ElMessage.info('修改密码功能正在开发中...')
    
    // 清空表单
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
  } catch (error) {
    console.error('Change password error:', error)
  }
}

// 处理删除账户
const handleDeleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '删除账户将永久清除您的所有数据，包括项目、文件等。此操作不可恢复！',
      '确认删除账户',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error',
        buttonSize: 'default'
      }
    )
    
    ElMessage.info('删除账户功能正在开发中...')
    
  } catch (error) {
    // 用户取消删除
  }
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      // 已在个人资料页面
      break
    case 'settings':
      ElMessage.info('设置功能正在开发中...')
      break
    case 'logout':
      userStore.logout()
      break
  }
}

// 组件挂载时初始化数据
onMounted(async () => {
  // 确保用户数据存在
  if (!userStore.user) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      router.push('/login')
      return
    }
  }
  
  initFormData()
})
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
  display: flex;
  align-items: center;
}

:deep(.el-tabs__nav-wrap) {
  padding: 0;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .max-w-4xl {
    margin: 0 16px;
  }
  
  .px-6 {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>