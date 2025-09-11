import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { authAPI, type LoginForm, type UserInfo } from '@/api/auth'
import router from '@/router'

export interface User {
  id: number
  username: string
  email: string
  nickname?: string
  avatarUrl?: string
  status: number
  lastLoginAt?: string
  createdAt: string
  updatedAt: string
}

export const useAuthStore = defineStore(
  'auth',
  () => {
    // 状态
    const token = ref<string>('')
    const user = ref<User | null>(null)
    const isLoading = ref<boolean>(false)

    // 计算属性
    const isAuthenticated = computed(() => !!token.value && !!user.value)
    const userDisplayName = computed(() => user.value?.nickname || user.value?.username || '')

    // 登录操作
    const login = async (loginData: LoginForm) => {
      isLoading.value = true
      try {
        const response = await authAPI.login(loginData)
        const { token: authToken, userInfo } = response.data.data
        
        // 存储认证信息
        setAuth(authToken, userInfo)
        
        ElMessage.success(`登录成功！欢迎回来，${userDisplayName.value}`)
        
        // 检查是否有重定向地址
        const redirect = router.currentRoute.value.query.redirect as string
        const targetPath = redirect || '/dashboard'
        
        // 跳转到目标页面
        router.push(targetPath)
        
        return response
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败')
        throw error
      } finally {
        isLoading.value = false
      }
    }

    // 登出操作
    const logout = async () => {
      try {
        // 注意：后端暂未实现logout接口，直接进行前端清理
        // 清除本地状态
        clearAuth()
        ElMessage.success('已退出登录')
        
        // 跳转到登录页
        router.push('/login')
      } catch (error) {
        console.error('登出失败:', error)
      }
    }

    // 获取当前用户信息
    const fetchCurrentUser = async () => {
      if (!token.value) {
        throw new Error('未登录')
      }
      
      try {
        const response = await authAPI.getCurrentUser()
        const userInfo = response.data.data
        user.value = userInfo
        return userInfo
      } catch (error: any) {
        // Token可能已过期，清除认证状态
        if (error.code === 401) {
          clearAuth()
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
        }
        throw error
      }
    }

    // 检查认证状态
    const checkAuth = async () => {
      if (!token.value) {
        return false
      }
      
      try {
        await fetchCurrentUser()
        return true
      } catch (error) {
        clearAuth()
        return false
      }
    }

    // 基础操作
    const setAuth = (authToken: string, userData: User) => {
      token.value = authToken
      user.value = userData
    }

    const clearAuth = () => {
      token.value = ''
      user.value = null
    }

    const setLoading = (loading: boolean) => {
      isLoading.value = loading
    }

    const updateUser = (userData: Partial<User>) => {
      if (user.value) {
        user.value = { ...user.value, ...userData }
      }
    }

    return {
      // 状态
      token,
      user,
      isLoading,
      // 计算属性
      isAuthenticated,
      userDisplayName,
      // 业务操作
      login,
      logout,
      fetchCurrentUser,
      checkAuth,
      // 基础操作
      setAuth,
      clearAuth,
      setLoading,
      updateUser,
    }
  },
  {
    persist: true, // 持久化存储
  }
)
