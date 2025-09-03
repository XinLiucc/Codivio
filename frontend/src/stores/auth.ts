import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface User {
  id: number
  username: string
  email: string
  nickname?: string
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

    // 操作
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
      // 操作
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
