import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import router from '@/router'
import { authAPI, type User, type LoginRequest, type RegisterRequest } from '@/api/auth'

interface UserState {
  user: User | null
  token: string | null
  refreshToken: string | null
  isLoggedIn: boolean
  loading: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    user: null,
    token: Cookies.get('access_token') || null,
    refreshToken: Cookies.get('refresh_token') || null,
    isLoggedIn: !!Cookies.get('access_token'),
    loading: false
  }),

  getters: {
    // 获取用户头像
    userAvatar(): string {
      if (this.user?.avatarUrl) {
        return this.user.avatarUrl
      }
      // 默认头像：使用用户名首字母生成
      const initial = this.user?.displayName?.charAt(0) || this.user?.username?.charAt(0) || 'U'
      return `https://ui-avatars.com/api/?name=${initial}&background=3b82f6&color=fff&size=40`
    },

    // 获取显示名称
    displayName(): string {
      return this.user?.displayName || this.user?.username || '未知用户'
    }
  },

  actions: {
    /**
     * 设置用户token
     */
    setToken(token: string, refreshToken: string) {
      this.token = token
      this.refreshToken = refreshToken
      this.isLoggedIn = true
      
      // 保存到Cookie (7天过期)
      Cookies.set('access_token', token, { expires: 7 })
      Cookies.set('refresh_token', refreshToken, { expires: 7 })
    },

    /**
     * 设置用户信息
     */
    setUser(user: User) {
      this.user = user
    },

    /**
     * 用户登录
     */
    async login(loginData: LoginRequest) {
      try {
        this.loading = true
        const response = await authAPI.login(loginData)
        
        const { accessToken, refreshToken, user } = response.data
        
        // 保存token和用户信息
        this.setToken(accessToken, refreshToken)
        this.setUser(user)
        
        ElMessage.success('登录成功')
        
        // 跳转到首页或回到之前的页面
        const redirect = router.currentRoute.value.query.redirect as string
        router.push(redirect || '/')
        
        return response
      } catch (error) {
        console.error('Login failed:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户注册
     */
    async register(registerData: RegisterRequest) {
      try {
        this.loading = true
        const response = await authAPI.register(registerData)
        
        ElMessage.success('注册成功，请登录')
        
        // 注册成功后跳转到登录页
        router.push('/login')
        
        return response
      } catch (error) {
        console.error('Register failed:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户登出
     */
    logout() {
      this.user = null
      this.token = null
      this.refreshToken = null
      this.isLoggedIn = false
      
      // 清除Cookie
      Cookies.remove('access_token')
      Cookies.remove('refresh_token')
      
      // 跳转到登录页
      router.push('/login')
      
      ElMessage.success('已退出登录')
    },

    /**
     * 获取当前用户信息
     */
    async fetchUserInfo() {
      try {
        if (!this.token) {
          throw new Error('No token available')
        }
        
        const response = await authAPI.getCurrentUser()
        this.setUser(response.data)
        
        return response
      } catch (error) {
        console.error('Fetch user info failed:', error)
        // 如果获取用户信息失败，可能是token过期，清除登录状态
        this.logout()
        throw error
      }
    },

    /**
     * 更新用户信息
     */
    async updateUserInfo(updateData: { displayName?: string; bio?: string; avatarUrl?: string }) {
      try {
        this.loading = true
        const response = await authAPI.updateCurrentUser(updateData)
        
        // 更新本地用户信息
        this.setUser(response.data)
        
        ElMessage.success('用户信息更新成功')
        
        return response
      } catch (error) {
        console.error('Update user info failed:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 刷新token
     */
    async refreshAccessToken() {
      try {
        if (!this.refreshToken) {
          throw new Error('No refresh token available')
        }
        
        const response = await authAPI.refreshToken({
          refreshToken: this.refreshToken
        })
        
        const { accessToken, refreshToken, user } = response.data
        
        // 更新token和用户信息
        this.setToken(accessToken, refreshToken)
        this.setUser(user)
        
        return response
      } catch (error) {
        console.error('Refresh token failed:', error)
        // 刷新失败，清除登录状态
        this.logout()
        throw error
      }
    },

    /**
     * 初始化用户状态（应用启动时调用）
     */
    async initUserState() {
      if (this.token && this.isLoggedIn) {
        try {
          // 尝试获取用户信息
          await this.fetchUserInfo()
        } catch (error) {
          // 如果获取失败，尝试刷新token
          try {
            await this.refreshAccessToken()
            await this.fetchUserInfo()
          } catch (refreshError) {
            // 刷新也失败，清除登录状态
            this.logout()
          }
        }
      }
    }
  }
})