import request, { type ApiResponse } from '@/utils/request'

// 用户信息类型
export interface User {
  userId: number
  username: string
  email: string
  displayName: string
  avatarUrl: string | null
  bio: string | null
  emailVerified: boolean
  createdAt: string
}

// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应数据
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: User
}

// 注册请求参数
export interface RegisterRequest {
  username: string
  email: string
  password: string
  displayName?: string
  inviteCode?: string
  confirmPassword: string
  agreement: boolean
}

// 刷新Token请求参数
export interface RefreshTokenRequest {
  refreshToken: string
}

// 更新用户信息请求参数
export interface UpdateUserRequest {
  displayName?: string
  bio?: string
  avatarUrl?: string
}

/**
 * 认证相关API
 */
export const authAPI = {
  /**
   * 用户登录
   */
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/login', data)
  },

  /**
   * 用户注册
   */
  register(data: RegisterRequest): Promise<ApiResponse<User>> {
    return request.post('/auth/register', data)
  },

  /**
   * 刷新Token
   */
  refreshToken(data: RefreshTokenRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/refresh', data)
  },

  /**
   * 检查用户名是否可用
   */
  checkUsername(username: string): Promise<ApiResponse<boolean>> {
    return request.get('/auth/check-username', {
      params: { username }
    })
  },

  /**
   * 检查邮箱是否可用
   */
  checkEmail(email: string): Promise<ApiResponse<boolean>> {
    return request.get('/auth/check-email', {
      params: { email }
    })
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser(): Promise<ApiResponse<User>> {
    return request.get('/users/me')
  },

  /**
   * 更新当前用户信息
   */
  updateCurrentUser(data: UpdateUserRequest): Promise<ApiResponse<User>> {
    return request.put('/users/me', data)
  },

  /**
   * 获取指定用户信息
   */
  getUserById(userId: number): Promise<ApiResponse<User>> {
    return request.get(`/users/${userId}`)
  }
}

/**
 * 系统健康检查API
 */
export const healthAPI = {
  /**
   * 系统健康检查
   */
  checkHealth(): Promise<ApiResponse<{
    status: string
    timestamp: string
    service: string
    version: string
  }>> {
    return request.get('/health')
  },

  /**
   * 认证测试接口
   */
  authTest(): Promise<ApiResponse<string>> {
    return request.get('/health/auth-test')
  }
}