import http, { ApiResponse } from '@/utils/http'

// 定义认证相关的数据类型
// 为什么要定义类型？
// 1. TypeScript类型安全 - 编译时发现错误
// 2. IDE智能提示 - 更好的开发体验  
// 3. 代码文档 - 类型即文档，清晰易懂
export interface LoginForm {
  loginId: string  // 支持用户名或邮箱登录
  password: string
}

export interface RegisterForm {
  username: string
  email: string
  password: string
  confirmPassword: string
  nickname?: string
}

export interface UserInfo {
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

export interface UpdateProfileForm {
  email?: string
  nickname?: string
  avatarUrl?: string
}

export interface ChangePasswordForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
  expiresIn: number  // token有效期（秒）
}

/**
 * 认证相关API服务
 * 为什么要创建API服务层？
 * 1. 职责分离 - HTTP配置和具体API调用分开
 * 2. 复用性 - 多个组件可以共享同一个API方法
 * 3. 维护性 - API变更时只需修改一个地方
 * 4. 可测试性 - 便于进行单元测试
 */
export const authAPI = {
  /**
   * 用户登录
   * 对应后端接口: POST /api/v1/auth/login
   */
  login: (loginData: LoginForm) => {
    return http.post<ApiResponse<LoginResponse>>('/auth/login', loginData)
  },

  /**
   * 用户注册  
   * 对应后端接口: POST /api/v1/auth/register
   */
  register: (registerData: RegisterForm) => {
    return http.post<ApiResponse<UserInfo>>('/auth/register', registerData)
  },

  // 注意：后端暂未实现logout接口

  /**
   * 检查用户名是否可用
   * 对应后端接口: GET /api/v1/auth/check-username/{username}
   */
  checkUsername: (username: string) => {
    return http.get<ApiResponse<boolean>>(`/auth/check-username/${username}`)
  },

  /**
   * 检查邮箱是否可用
   * 对应后端接口: GET /api/v1/auth/check-email/{email}  
   */
  checkEmail: (email: string) => {
    return http.get<ApiResponse<boolean>>(`/auth/check-email/${email}`)
  },

  /**
   * 获取当前用户信息
   * 对应后端接口: GET /api/v1/users/me
   */
  getCurrentUser: () => {
    return http.get<ApiResponse<UserInfo>>('/users/me')
  },

  /**
   * 更新用户个人信息
   * 对应后端接口: PUT /api/v1/users/profile
   */
  updateProfile: (profileData: UpdateProfileForm) => {
    return http.put<ApiResponse<UserInfo>>('/users/profile', profileData)
  },

  /**
   * 修改用户密码
   * 对应后端接口: PUT /api/v1/users/password
   */
  changePassword: (passwordData: ChangePasswordForm) => {
    return http.put<ApiResponse<null>>('/users/password', passwordData)
  }
}

/**
 * 为什么这样组织API？
 * 
 * 1. 命名规范 - 动词+名词的方式，语义清晰
 * 2. 参数类型化 - 每个方法的参数都有明确的类型定义
 * 3. 返回类型化 - 指定了返回数据的具体类型
 * 4. 注释完整 - 标明对应的后端接口路径
 * 5. 分类明确 - 认证相关的API都在authAPI对象中
 * 
 * 使用示例:
 * ```typescript
 * try {
 *   const response = await authAPI.login({ loginId: 'john', password: '123456' })
 *   console.log('登录成功:', response.data.data.userInfo)
 * } catch (error) {
 *   console.error('登录失败:', error)
 * }
 * ```
 */